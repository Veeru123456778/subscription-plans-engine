# FirstClub Membership Program — Technical Specification

**Status:** v1 — Draft
**Companion doc:** `requirements.md` (v1, Finalized) — every section here traces back to an FR/NFR from that doc
**Last updated:** 2026-08-13

---

## 1. Architecture Overview

### 1.1 Service boundary

The Membership Program is modeled as a standalone **Membership Service**, exposing REST
APIs to (a) the client apps (web/mobile) and (b) other internal services (Order Service,
Checkout Service). It owns its own datastore and does not directly own Users, Orders, or
Cohorts — those are read via internal APIs from their owning services.

```
                         ┌────────────────────┐
                         │   Client (App/Web)  │
                         └─────────┬───────────┘
                                   │ REST (HTTPS)
                                   ▼
┌──────────────────────────────────────────────────────────┐
│                    Membership Service                      │
│                                                              │
│  ┌───────────┐  ┌───────────┐  ┌────────────────────────┐ │
│  │  Plan API  │  │ Tier/     │  │ Subscription Lifecycle │ │
│  │            │  │ Benefit   │  │ API (subscribe/cancel/ │ │
│  │            │  │ Admin API │  │ upgrade/downgrade)     │ │
│  └───────────┘  └───────────┘  └────────────────────────┘ │
│                                                              │
│  ┌────────────────────────────────────────────────────┐   │
│  │        Tier Evaluation Engine (lazy + cache)         │   │
│  └────────────────────────────────────────────────────┘   │
│                          │                                  │
│                          ▼                                  │
│                 ┌─────────────────┐                         │
│                 │  Cache (Redis)   │  key: tier:{userId}    │
│                 │  TTL: 1 hour     │  fixed, hardcoded       │
│                 └─────────────────┘                         │
│                                                              │
│                 ┌─────────────────┐                         │
│                 │  Primary DB      │                        │
│                 │  (Postgres)      │                        │
│                 └─────────────────┘                         │
└───────────────────┬──────────────────┬──────────────────────┘
                     │                  │
       ┌─────────────▼───┐    ┌─────────▼──────────┐
       │  Order Service    │    │  Razorpay (test)    │
       │  (order count/    │    │  Payment Gateway    │
       │   value, via API) │    │                      │
       └───────────────────┘    └──────────────────────┘
                     │
       ┌─────────────▼───┐
       │ User/Cohort       │
       │ Service (reads    │
       │ cohort_id/tags)   │
       └───────────────────┘

Checkout Service → calls Membership Service's
"GET /internal/benefits/{userId}" at checkout time (FR-5)
```

### 1.2 Key architectural decisions (traced to requirements)

| Decision | Why | Traces to |
|---|---|---|
| Tier computed lazily, cached in Redis with fixed 1h TTL, not a batch cron job | Avoids scanning entire member base on a schedule; recompute only happens when actually needed | FR-11, Decisions Log #1 |
| Paid upgrade / voluntary downgrade bypass cache and write-through immediately | These are explicit user actions and must reflect instantly | FR-9, FR-10 |
| Benefits stored as structured, typed rows (not hardcoded columns) | Admin needs to configure benefit types/values without a deploy | FR-3, Assumption 2 |
| Tier-upgrade pricing stored as a matrix table keyed by (plan_id, from_tier, to_tier) | Price varies by both plan and tier-pair | FR-9, Assumption 4 |
| Order count/value and cohort read via API from external services, not duplicated | Single source of truth; Membership Service doesn't own Orders/Cohorts | Assumption 3 |
| One active Membership per user (DB-level uniqueness) | No stacked memberships allowed | Assumption 7 |
| No refund logic anywhere in this service | Simplifies cancel/downgrade to a pure status change | FR-12, Decisions Log #4 |

---

## 2. Data Model

### 2.1 Entity-Relationship overview

```
   Plan (1) ────────< (M) Membership (M) >──────── (1) User [external]
     │                        │
     │                        │ current_tier_id
     │                        ▼
     │                      Tier (1)───< (M) TierBenefit >───(1) Benefit
     │                        │
     │                        │
     └──< (M) TierUpgradePrice >── keyed by (plan_id, from_tier_id, to_tier_id)
                                │
                              Tier (self-referencing via from/to)

  Tier (1) ───< (M) TierCriteria

  Membership (1) ───< (M) MembershipAuditLog

  TierCache (Redis, not relational) — key: tier:{userId} → {tierId, computedAt, source}
```

### 2.2 Entities

#### `Plan`
Billing plan a user subscribes to.

| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `name` | string | "Monthly", "Quarterly", "Yearly" |
| `duration_days` | int | e.g. 30, 90, 365 |
| `price` | decimal | |
| `currency` | string | e.g. "INR" |
| `is_active` | boolean | inactive plans hidden from users (FR-1) but preserved for existing subscribers (FR-2) |
| `created_at` / `updated_at` | timestamp | |

*Traces to: FR-1, FR-2*

#### `Tier`
A benefit level. Fixed set at launch (Silver/Gold/Platinum) but modeled as a table, not an enum, so tiers themselves could be added later without a schema change.

| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `name` | string | "Silver", "Gold", "Platinum" |
| `rank` | int | ordering, e.g. Silver=1, Gold=2, Platinum=3 — used to determine "higher/lower" for upgrade/downgrade validation |
| `is_active` | boolean | |

#### `TierCriteria`
Rules used to auto-compute a user's tier (FR-11).

| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `tier_id` | UUID (FK → Tier) | which tier this criteria set unlocks |
| `min_order_count` | int, nullable | e.g. "more than X orders" |
| `min_order_value_monthly` | decimal, nullable | e.g. total order value in a month |
| `cohort_tags` | string[], nullable | matches against user's cohort tags |
| `match_mode` | enum(`ANY`, `ALL`) | whether meeting any one condition qualifies, or all must be met |
| `is_active` | boolean | |

*A user qualifies for a tier if their computed stats satisfy this tier's criteria. The engine evaluates all active tiers' criteria and assigns the highest-rank tier the user qualifies for (see §4 Tier Evaluation Engine).*

#### `Benefit`
A configurable perk definition (admin-managed), independent of any specific tier — a "catalog" of benefit instances.

| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `type` | enum(`FREE_DELIVERY`, `DISCOUNT_PERCENT`, `EARLY_ACCESS`, `PRIORITY_SUPPORT`) | launch set, extensible (Assumption 2) |
| `value` | JSON | shape depends on `type` — e.g. `{"min_order_value": 499}` for FREE_DELIVERY, `{"percent": 10}` for DISCOUNT_PERCENT |
| `scope` | JSON, nullable | e.g. `{"category_ids": [...]}` or `{"item_ids": [...]}` — null means unscoped/global |
| `is_active` | boolean | |

#### `TierBenefit`
Join table — which Benefits apply to which Tier.

| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `tier_id` | UUID (FK → Tier) | |
| `benefit_id` | UUID (FK → Benefit) | |
| `is_active` | boolean | allows disabling a benefit for a tier without deleting the Benefit definition |

*Traces to: FR-3, FR-4, FR-5*

#### `TierUpgradePrice`
The pricing matrix for paid tier upgrades (FR-9).

| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `plan_id` | UUID (FK → Plan) | |
| `from_tier_id` | UUID (FK → Tier) | |
| `to_tier_id` | UUID (FK → Tier) | must have higher `rank` than `from_tier_id` |
| `price` | decimal | |
| `currency` | string | |
| `is_active` | boolean | |

- **Uniqueness constraint:** `(plan_id, from_tier_id, to_tier_id)` — one price per combination.
- **Absence = unavailable.** If no active row exists for a given `(plan_id, from_tier_id, to_tier_id)`, that upgrade path is not offered.

#### `Membership`
The core subscription record for a user.

| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `user_id` | UUID | FK to external User |
| `plan_id` | UUID (FK → Plan) | |
| `current_tier_id` | UUID (FK → Tier) | the tier currently in effect (auto or paid) |
| `tier_source` | enum(`AUTO`, `PAID_UPGRADE`) | determines whether lazy auto-eval is allowed to override it downward (FR-11) |
| `status` | enum(`ACTIVE`, `CANCELLED`, `EXPIRED`) | |
| `start_date` | timestamp | |
| `expiry_date` | timestamp | |
| `created_at` / `updated_at` | timestamp | |

- **Uniqueness constraint:** at most one row with `status = ACTIVE` per `user_id` (Assumption 7).

*Traces to: FR-7, FR-8, FR-9, FR-10, FR-12, FR-13, FR-14*

#### `MembershipAuditLog`
Append-only log of all Plan/Tier changes, per NFR "Auditability."

| Field | Type | Notes |
|---|---|---|
| `id` | UUID (PK) | |
| `membership_id` | UUID (FK → Membership) | |
| `event_type` | enum(`SUBSCRIBED`, `PLAN_CHANGED`, `TIER_AUTO_UPGRADED`, `TIER_PAID_UPGRADED`, `TIER_DOWNGRADED`, `CANCELLED`, `EXPIRED`) | |
| `before_state` | JSON | |
| `after_state` | JSON | |
| `triggered_by` | enum(`USER`, `SYSTEM`, `ADMIN`) | |
| `created_at` | timestamp | |

#### `TierCache` (Redis — not relational)

| Key | Value | TTL |
|---|---|---|
| `tier:{userId}` | `{ "tierId": "...", "computedAt": "...", "source": "AUTO\|PAID_UPGRADE" }` | fixed 1 hour |

- Written on every fresh computation (subscribe, cache-miss read, paid upgrade, downgrade).
- Explicitly deleted/overwritten (not waited-out) on paid upgrade and voluntary downgrade — see §4.

---

## 3. API Design

All endpoints are prefixed `/api/v1`. User-facing endpoints require an authenticated user
session; Admin endpoints require an authenticated admin role. Internal endpoints are
callable only by other trusted internal services (e.g. Checkout).

### 3.1 Plans

| Method | Path | Purpose | Traces to |
|---|---|---|---|
| GET | `/plans` | List all active plans, with price/duration/benefit summary | FR-1, FR-6 |
| POST | `/admin/plans` | Create a plan | FR-2 |
| PATCH | `/admin/plans/{planId}` | Edit a plan (price, duration, active status) | FR-2 |
| DELETE | `/admin/plans/{planId}` | Disable a plan (soft-delete, sets `is_active=false`) | FR-2 |

**`GET /plans` response example:**
```json
{
  "plans": [
    {
      "id": "plan_monthly",
      "name": "Monthly",
      "durationDays": 30,
      "price": 199,
      "currency": "INR"
    }
  ]
}
```

### 3.2 Tiers & Benefits

| Method | Path | Purpose | Traces to |
|---|---|---|---|
| GET | `/tiers` | List all tiers with their current benefit configuration | FR-6 |
| GET | `/membership/benefits` | Get the caller's current tier + benefits | FR-4 |
| POST | `/admin/tiers` | Create a tier | FR-3 |
| POST | `/admin/tiers/{tierId}/benefits` | Attach a Benefit to a Tier | FR-3 |
| PATCH | `/admin/tiers/{tierId}/benefits/{tierBenefitId}` | Enable/disable/edit a tier's benefit | FR-3 |
| POST | `/admin/benefits` | Create a new Benefit definition (`type`, `value`, `scope`) | FR-3 |
| PATCH | `/admin/benefits/{benefitId}` | Edit a Benefit definition | FR-3 |
| POST | `/admin/tiers/{tierId}/criteria` | Set/update auto-tier criteria for a tier | FR-11 |
| POST | `/admin/tier-upgrade-prices` | Create a `(plan, fromTier, toTier)` price entry | FR-9 |
| PATCH | `/admin/tier-upgrade-prices/{id}` | Edit/disable a price entry | FR-9 |

### 3.3 Subscription Lifecycle

| Method | Path | Purpose | Traces to |
|---|---|---|---|
| GET | `/membership` | Get caller's current membership (plan, tier, expiry) | FR-13 |
| POST | `/membership/subscribe` | Subscribe to a plan (creates payment order, then Membership on success) | FR-7 |
| POST | `/membership/change-plan` | Change to a different plan | FR-8 |
| POST | `/membership/upgrade-tier` | Pay to upgrade to a specific target tier | FR-9 |
| POST | `/membership/downgrade-tier` | Voluntarily downgrade to a lower tier | FR-10 |
| POST | `/membership/cancel` | Cancel membership immediately | FR-12 |

**`POST /membership/subscribe` request/response:**
```json
// Request
{ "planId": "plan_yearly" }

// Response (payment required first)
{
  "razorpayOrderId": "order_xyz",
  "amount": 1999,
  "currency": "INR",
  "keyId": "rzp_test_xxx"
}
```
Client completes payment via Razorpay Checkout SDK using the returned order, then calls:

| Method | Path | Purpose |
|---|---|---|
| POST | `/membership/subscribe/confirm` | Verify Razorpay payment signature, create Membership |

```json
// Request
{
  "razorpayOrderId": "order_xyz",
  "razorpayPaymentId": "pay_abc",
  "razorpaySignature": "..."
}

// Response
{
  "membershipId": "mem_123",
  "planId": "plan_yearly",
  "tierId": "tier_silver",
  "tierSource": "AUTO",
  "status": "ACTIVE",
  "startDate": "2026-08-13T00:00:00Z",
  "expiryDate": "2027-08-13T00:00:00Z"
}
```

**`POST /membership/upgrade-tier` request:**
```json
{ "targetTierId": "tier_gold" }
```
Follows the same two-step pattern (create Razorpay order → confirm) as subscribe, since it's also a paid action. On confirm, `current_tier_id` is set to `targetTierId`, `tier_source = PAID_UPGRADE`, and the tier cache is invalidated immediately.

**`POST /membership/downgrade-tier` request:**
```json
{ "targetTierId": "tier_silver" }
```
No payment involved — takes effect immediately, cache invalidated immediately.

### 3.4 Internal APIs (service-to-service)

| Method | Path | Purpose | Traces to |
|---|---|---|---|
| GET | `/internal/benefits/{userId}` | Called by Checkout Service to fetch a user's currently applicable benefits, to apply at checkout | FR-5 |

**`GET /internal/benefits/{userId}` response:**
```json
{
  "hasActiveMembership": true,
  "tierId": "tier_gold",
  "benefits": [
    { "type": "FREE_DELIVERY", "value": { "minOrderValue": 0 } },
    { "type": "DISCOUNT_PERCENT", "value": { "percent": 10 }, "scope": { "categoryIds": ["electronics"] } }
  ]
}
```
This is the read path that triggers lazy tier evaluation/caching (see §4). If `hasActiveMembership` is `false` (no active Membership, or expired), Checkout applies no membership benefits (FR-5).

---

## 4. Tier Evaluation Engine (Lazy + Cached)

Central to this system — implemented once, called from every read path (`GET /membership`,
`GET /membership/benefits`, `GET /internal/benefits/{userId}`, and internally at subscribe time).

### 4.1 `resolveTier(userId)` — pseudocode

```
function resolveTier(userId):
    membership = getActiveMembership(userId)
    if membership is null:
        return NO_ACTIVE_MEMBERSHIP

    if membership.tier_source == PAID_UPGRADE:
        # Paid upgrades are sticky — never silently overridden by auto-eval.
        # Cache still used to avoid repeated DB reads, but source of truth
        # is the Membership row itself, not recomputation.
        cached = redis.get("tier:" + userId)
        if cached and cached.tierId == membership.current_tier_id:
            return cached
        cached = { tierId: membership.current_tier_id, computedAt: now(), source: PAID_UPGRADE }
        redis.set("tier:" + userId, cached, ttl=1h)
        return cached

    cached = redis.get("tier:" + userId)
    if cached exists and not expired:
        return cached

    # Cache miss or expired -> recompute
    stats = fetchUserStats(userId)   # order count, order value (via Order Service API)
    cohort = fetchUserCohort(userId) # via User/Cohort Service API
    newTierId = computeTierFromCriteria(stats, cohort)  # highest-rank tier whose criteria is met

    if newTierId != membership.current_tier_id:
        updateMembershipTier(membership.id, newTierId, source=AUTO)
        writeAuditLog(membership.id, TIER_AUTO_UPGRADED, before=membership.current_tier_id, after=newTierId)

    result = { tierId: newTierId, computedAt: now(), source: AUTO }
    redis.set("tier:" + userId, result, ttl=1h)
    return result
```

### 4.2 `computeTierFromCriteria(stats, cohort)`

- Iterate all active Tiers ordered by `rank` descending.
- For each tier, evaluate its `TierCriteria` row(s) against `stats`/`cohort` using `match_mode` (`ANY`/`ALL`).
- Return the **highest-rank** tier whose criteria is satisfied. If none match, default to the lowest-rank active tier (e.g. Silver) as the floor for any active member.

### 4.3 Cache invalidation (bypass paths)

Two write paths **do not wait for TTL** — they invalidate/overwrite the cache synchronously as part of the same transaction:

| Action | Effect |
|---|---|
| `POST /membership/upgrade-tier` (confirm) | `current_tier_id = target`, `tier_source = PAID_UPGRADE`, cache overwritten immediately |
| `POST /membership/downgrade-tier` | `current_tier_id = target`, `tier_source = AUTO` (a voluntary downgrade drops back to auto-eval eligibility going forward), cache overwritten immediately |

> Design note: setting `tier_source = AUTO` after a voluntary downgrade means the user could
> auto-upgrade right back up on the next lazy evaluation if they still meet the higher
> tier's criteria. This mirrors real-world intent (you can't "downgrade" your actual
> purchase history) — flagging this as a behavior worth confirming with product before
> implementation, since a user might expect the downgrade to "stick."

---

## 5. Key Flows

### 5.1 Subscribe to a Plan (FR-7)

1. Client → `POST /membership/subscribe { planId }`.
2. Service validates: no existing `ACTIVE` membership for this user (if one exists → 409, points client to `change-plan`).
3. Service creates a Razorpay Order for the plan's price, returns order details to client.
4. Client completes payment via Razorpay Checkout SDK (test mode).
5. Client → `POST /membership/subscribe/confirm { razorpayOrderId, razorpayPaymentId, razorpaySignature }`.
6. Service verifies signature server-side against Razorpay.
7. On success (in a single DB transaction):
   - Create `Membership` row: `status=ACTIVE`, `start_date=now`, `expiry_date=now+plan.duration_days`, `tier_source=AUTO`.
   - Call `resolveTier(userId)` to compute and cache the initial tier from existing history.
   - Write `MembershipAuditLog` (`SUBSCRIBED`).
8. Return the created Membership to client.
9. On payment verification failure: no Membership created, return 402 with reason.

### 5.2 Checkout-time benefit application (FR-5)

1. Checkout Service, while building the cart total, calls `GET /internal/benefits/{userId}`.
2. Membership Service calls `resolveTier(userId)` (cache hit in the common case).
3. If no active membership → `{ hasActiveMembership: false }`, Checkout applies nothing.
4. If active → fetch that tier's active `TierBenefit`s, apply `scope` filtering, return benefit list.
5. Checkout applies FREE_DELIVERY / DISCOUNT_PERCENT to eligible line items per the returned scope; EARLY_ACCESS/PRIORITY_SUPPORT are entitlement flags read by their respective features, not by Checkout.

### 5.3 Paid Tier Upgrade (FR-9)

1. Client → `POST /membership/upgrade-tier { targetTierId }`.
2. Service validates: active membership exists; `targetTierId.rank > currentTier.rank`; a `TierUpgradePrice` row exists and is active for `(membership.plan_id, currentTier, targetTier)` — else 404 "upgrade path unavailable."
3. Service creates Razorpay Order for that price, returns to client.
4. Client completes payment.
5. Client → `POST /membership/upgrade-tier/confirm {...payment fields...}`.
6. On verified success: update `current_tier_id`, `tier_source=PAID_UPGRADE`; invalidate/overwrite cache immediately; write audit log (`TIER_PAID_UPGRADED`).

### 5.4 Voluntary Downgrade (FR-10)

1. Client → `POST /membership/downgrade-tier { targetTierId }`.
2. Service validates: active membership; `targetTierId.rank < currentTier.rank`.
3. Update `current_tier_id = targetTierId`, `tier_source = AUTO`; invalidate/overwrite cache immediately; write audit log (`TIER_DOWNGRADED`). No payment/refund logic involved.

### 5.5 Cancel Membership (FR-12)

1. Client → `POST /membership/cancel`.
2. Service sets `status = CANCELLED` on the active Membership. No refund logic, no proration.
3. Delete/invalidate the tier cache entry for this user (so a stale cached tier doesn't leak into `resolveTier` returning `hasActiveMembership: false` inconsistently — the membership status check on the DB row is the actual source of truth for "active or not," cache is only ever for computed tier value).
4. Write audit log (`CANCELLED`).

### 5.6 Expiry (FR-14)

1. Scheduled job (frequency: hourly or daily — implementation detail, not user-facing) queries `Membership WHERE status='ACTIVE' AND expiry_date < now()`.
2. For each: set `status = EXPIRED`; write audit log (`EXPIRED`).
3. No charge attempted (no auto-renew per FR-14). No notification sent (out of scope per Decisions Log #7).

### 5.7 Change Plan (FR-8)

1. Client → `POST /membership/change-plan { newPlanId }`.
2. If new plan's price differs from a straightforward swap, service creates a payment order for any required adjustment (exact proration logic: **flagged as an open design question**, see §7).
3. On success: update `plan_id`, recompute `expiry_date` from `now + newPlan.duration_days`. `current_tier_id`/`tier_source` are **not** touched — tier is independent of plan.

---

## 6. Error Handling & Edge Cases

| Case | Handling |
|---|---|
| Double-subscribe attempt (user already has ACTIVE membership) | `409 Conflict`, response points client to `change-plan` |
| Razorpay webhook/confirm called twice for the same payment (retry) | Idempotency: confirm endpoints check if a Membership/upgrade already exists for that `razorpayPaymentId` before creating a duplicate; safe to return the existing result |
| Payment fails or signature verification fails | No state mutation; `402 Payment Required` with reason; no Membership/upgrade is created |
| Upgrade requested to a tier with no configured price for the user's plan | `404 Not Found` — "upgrade path unavailable" |
| Upgrade/downgrade requested to the same tier the user already has | `400 Bad Request` |
| Downgrade requested to a *higher* tier via the downgrade endpoint (misuse) | `400 Bad Request` — must use `upgrade-tier` |
| Concurrent requests racing to mutate the same Membership (e.g. cancel + upgrade at once) | DB-level optimistic locking (`updated_at`/version column) on `Membership`; losing request gets `409 Conflict`, client retries |
| Order Service or Cohort Service unavailable during tier recomputation | Fall back to the **last cached/known tier** rather than failing the read entirely (degrade gracefully — checkout must not break because tier computation failed); log the failure for alerting |
| Cache (Redis) unavailable | Fall back to computing directly from DB every time (slower but correct); do not fail requests outright |

---

## 7. Open Design Questions (flagged, not yet resolved)

These surfaced while writing this spec and aren't answered by `requirements.md` — worth a quick decision before implementation starts:

1. **Change-Plan proration:** FR-8 says "complete any required payment adjustment" but the exact proration formula (e.g. mid-cycle Monthly→Yearly switch) isn't defined. Needs a decision: full new-plan price, prorated credit, or flat fee?
2. **Voluntary downgrade + auto re-upgrade:** per §4.3, setting `tier_source=AUTO` after a downgrade means the user could be auto-upgraded right back on the next lazy read if their order history still qualifies. Confirm this is the intended behavior.
3. **Multiple active TierCriteria per tier:** if a tier has more than one `TierCriteria` row (e.g. one for orders, one for cohort), is satisfying *any* row enough, or does `match_mode` apply *across* rows too? Current design assumes `match_mode` is per-row (fields within one row), and multiple rows for the same tier are OR'd together — needs confirmation.
4. **Grace/floor tier for zero-history users:** confirmed default is "lowest active tier" (e.g. Silver) — worth an explicit product sign-off since it wasn't stated in requirements.md.

---

## 8. Traceability Summary

| Requirements Doc Section | Tech Spec Section |
|---|---|
| FR-1, FR-2 (Plans) | §3.1 Plans API |
| FR-3, FR-4, FR-5 (Benefits) | §2.2 Benefit/TierBenefit entities, §3.2 API, §5.2 Checkout flow |
| FR-6 (Get plans & tiers) | §3.1, §3.2 |
| FR-7 (Subscribe) | §5.1 |
| FR-8 (Change plan) | §5.7 |
| FR-9 (Paid tier upgrade) | §2.2 TierUpgradePrice, §5.3 |
| FR-10 (Voluntary downgrade) | §5.4 |
| FR-11 (Auto tier assignment) | §4 Tier Evaluation Engine |
| FR-12 (Cancel) | §5.5 |
| FR-13 (Track membership) | §3.3 `GET /membership` |
| FR-14 (Expiry) | §5.6 |
| NFR: Auditability | §2.2 MembershipAuditLog |
| NFR: Idempotency | §6 Error Handling |
| NFR: Cache (fixed 1h TTL) | §2.2 TierCache, §4 |

---

## 9. Application Structure (Java + Spring Boot)

### 9.1 Package strategy: feature-first, not layer-first

Two common approaches exist for a Spring Boot codebase:
- **Package-by-layer** (`controller/`, `service/`, `repository/` at the top level, with all entities dumped inside each) — gets messy fast once you have 6+ domain entities like this project does; a change to "Tier" touches four different top-level folders.
- **Package-by-feature** (each domain concept owns its own vertical slice) — a change to "Tier" logic stays inside `tier/`. This scales better and matches how the data model in §2 is already organized, so it's what's used below.

Within each feature package, the internal layering (`controller → service → repository`) is still enforced — just scoped to that feature, not spread across the whole app.

### 9.2 Folder structure

```
firstclub-membership-service/
├── pom.xml (or build.gradle)
├── src/
│   ├── main/
│   │   ├── java/com/firstclub/membership/
│   │   │   ├── MembershipServiceApplication.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java          # auth for user/admin/internal routes
│   │   │   │   ├── RedisConfig.java             # tier cache client + fixed 1h TTL bean
│   │   │   │   ├── RazorpayConfig.java          # Razorpay client bean, key/secret binding
│   │   │   │   ├── OpenApiConfig.java           # Swagger/OpenAPI docs
│   │   │   │   ├── WebClientConfig.java         # HTTP clients for Order/Cohort services
│   │   │   │   └── SchedulerConfig.java         # enables @Scheduled for expiry job
│   │   │   │
│   │   │   ├── common/
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java   # @ControllerAdvice, maps to §6 error table
│   │   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   │   ├── ConflictException.java        # e.g. double-subscribe (409)
│   │   │   │   │   ├── PaymentFailedException.java
│   │   │   │   │   └── InvalidTierTransitionException.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── ApiResponse.java              # standard response envelope
│   │   │   │   │   └── ErrorResponse.java
│   │   │   │   ├── util/
│   │   │   │   │   ├── DateUtils.java
│   │   │   │   │   └── IdGenerator.java
│   │   │   │   └── enums/
│   │   │   │       ├── MembershipStatus.java         # ACTIVE, CANCELLED, EXPIRED
│   │   │   │       └── TierSource.java                # AUTO, PAID_UPGRADE
│   │   │   │
│   │   │   ├── plan/                                  # traces to FR-1, FR-2 / §2.2 Plan / §3.1
│   │   │   │   ├── controller/
│   │   │   │   │   ├── PlanController.java            # GET /plans
│   │   │   │   │   └── PlanAdminController.java        # POST/PATCH/DELETE /admin/plans
│   │   │   │   ├── service/
│   │   │   │   │   ├── PlanService.java (interface)
│   │   │   │   │   └── impl/PlanServiceImpl.java
│   │   │   │   ├── repository/PlanRepository.java
│   │   │   │   ├── entity/Plan.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── PlanResponse.java
│   │   │   │   │   ├── CreatePlanRequest.java
│   │   │   │   │   └── UpdatePlanRequest.java
│   │   │   │   └── mapper/PlanMapper.java              # MapStruct: entity <-> dto
│   │   │   │
│   │   │   ├── tier/                                   # traces to §2.2 Tier, TierCriteria / §3.2
│   │   │   │   ├── controller/
│   │   │   │   │   ├── TierController.java             # GET /tiers
│   │   │   │   │   └── TierAdminController.java        # POST /admin/tiers, /criteria
│   │   │   │   ├── service/
│   │   │   │   │   ├── TierService.java
│   │   │   │   │   └── impl/TierServiceImpl.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── TierRepository.java
│   │   │   │   │   └── TierCriteriaRepository.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Tier.java
│   │   │   │   │   └── TierCriteria.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── TierResponse.java
│   │   │   │   │   └── TierCriteriaRequest.java
│   │   │   │   └── mapper/TierMapper.java
│   │   │   │
│   │   │   ├── benefit/                                # traces to FR-3, FR-4 / §2.2 Benefit, TierBenefit
│   │   │   │   ├── controller/
│   │   │   │   │   ├── BenefitController.java          # GET /membership/benefits
│   │   │   │   │   └── BenefitAdminController.java     # POST /admin/benefits, tier-benefits
│   │   │   │   ├── service/
│   │   │   │   │   ├── BenefitService.java
│   │   │   │   │   └── impl/BenefitServiceImpl.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── BenefitRepository.java
│   │   │   │   │   └── TierBenefitRepository.java
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Benefit.java
│   │   │   │   │   └── TierBenefit.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── BenefitResponse.java
│   │   │   │   │   └── CreateBenefitRequest.java
│   │   │   │   └── mapper/BenefitMapper.java
│   │   │   │
│   │   │   ├── pricing/                                # traces to FR-9 / §2.2 TierUpgradePrice
│   │   │   │   ├── controller/TierUpgradePriceAdminController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── TierUpgradePriceService.java
│   │   │   │   │   └── impl/TierUpgradePriceServiceImpl.java
│   │   │   │   ├── repository/TierUpgradePriceRepository.java
│   │   │   │   ├── entity/TierUpgradePrice.java
│   │   │   │   └── dto/TierUpgradePriceRequest.java
│   │   │   │
│   │   │   ├── membership/                             # traces to FR-7,8,9,10,12,13,14 / §5 flows
│   │   │   │   ├── controller/
│   │   │   │   │   └── MembershipController.java       # subscribe, change-plan, upgrade,
│   │   │   │   │                                        # downgrade, cancel, GET /membership
│   │   │   │   ├── service/
│   │   │   │   │   ├── MembershipService.java
│   │   │   │   │   └── impl/MembershipServiceImpl.java  # orchestrates payment + tier engine
│   │   │   │   ├── repository/MembershipRepository.java
│   │   │   │   ├── entity/Membership.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── SubscribeRequest.java
│   │   │   │   │   ├── ChangePlanRequest.java
│   │   │   │   │   ├── UpgradeTierRequest.java
│   │   │   │   │   ├── DowngradeTierRequest.java
│   │   │   │   │   ├── PaymentConfirmRequest.java
│   │   │   │   │   └── MembershipResponse.java
│   │   │   │   ├── mapper/MembershipMapper.java
│   │   │   │   └── validator/
│   │   │   │       └── TierTransitionValidator.java     # rank checks for upgrade/downgrade
│   │   │   │
│   │   │   ├── evaluation/                             # traces to FR-11 / §4 Tier Evaluation Engine
│   │   │   │   ├── TierEvaluationEngine.java            # resolveTier() — the core algorithm
│   │   │   │   ├── TierCriteriaMatcher.java             # computeTierFromCriteria()
│   │   │   │   ├── cache/
│   │   │   │   │   ├── TierCacheService.java            # get/set/invalidate, wraps Redis
│   │   │   │   │   └── TierCacheEntry.java              # cache value DTO
│   │   │   │   └── dto/UserStatsSnapshot.java           # order count/value + cohort, assembled
│   │   │   │                                             # from external client calls
│   │   │   │
│   │   │   ├── payment/                                # Razorpay integration, isolated behind
│   │   │   │   ├── RazorpayClientAdapter.java           # thin wrapper over Razorpay SDK
│   │   │   │   ├── service/
│   │   │   │   │   ├── PaymentOrderService.java         # create order
│   │   │   │   │   └── PaymentVerificationService.java  # verify signature (idempotent)
│   │   │   │   ├── dto/
│   │   │   │   │   ├── PaymentOrderResponse.java
│   │   │   │   │   └── PaymentConfirmation.java
│   │   │   │   └── entity/PaymentRecord.java             # tracks razorpayOrderId/paymentId
│   │   │   │                                              # for idempotency (§6)
│   │   │   │
│   │   │   ├── audit/                                   # traces to §2.2 MembershipAuditLog / NFR
│   │   │   │   ├── AuditLogService.java
│   │   │   │   ├── entity/MembershipAuditLog.java
│   │   │   │   ├── repository/MembershipAuditLogRepository.java
│   │   │   │   └── enums/AuditEventType.java
│   │   │   │
│   │   │   ├── client/                                  # outbound calls to OTHER services
│   │   │   │   ├── order/
│   │   │   │   │   ├── OrderServiceClient.java           # interface
│   │   │   │   │   ├── OrderServiceClientImpl.java        # WebClient-based impl
│   │   │   │   │   └── dto/UserOrderStats.java
│   │   │   │   └── cohort/
│   │   │   │       ├── CohortServiceClient.java
│   │   │   │       ├── CohortServiceClientImpl.java
│   │   │   │       └── dto/UserCohortInfo.java
│   │   │   │
│   │   │   ├── internal/                                 # inbound APIs for OTHER internal
│   │   │   │   └── controller/                            # services (e.g. Checkout) to call
│   │   │   │       └── InternalBenefitController.java     # GET /internal/benefits/{userId}
│   │   │   │
│   │   │   └── scheduler/
│   │   │       └── MembershipExpiryJob.java               # traces to FR-14 / §5.6
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-staging.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/                              # Flyway
│   │           ├── V1__create_plan_table.sql
│   │           ├── V2__create_tier_and_criteria_tables.sql
│   │           ├── V3__create_benefit_and_tier_benefit_tables.sql
│   │           ├── V4__create_tier_upgrade_price_table.sql
│   │           ├── V5__create_membership_table.sql
│   │           ├── V6__create_membership_audit_log_table.sql
│   │           └── V7__create_payment_record_table.sql
│   │
│   └── test/
│       └── java/com/firstclub/membership/
│           ├── plan/... (mirrors main structure per feature)
│           ├── membership/
│           │   ├── MembershipServiceImplTest.java          # unit, mocked deps
│           │   └── MembershipControllerIntegrationTest.java # @SpringBootTest + Testcontainers
│           ├── evaluation/
│           │   └── TierEvaluationEngineTest.java            # covers §4 pseudocode branches
│           └── support/
│               ├── TestcontainersConfig.java                 # Postgres + Redis containers
│               └── fixtures/                                  # test data builders
```

### 9.3 Verification against §2 (Data Model) and §3 (API Design)

Cross-checking that every entity and endpoint from earlier sections has an explicit home:

| From §2 / §3 | Lives in |
|---|---|
| `Plan` entity + `/plans`, `/admin/plans` | `plan/` |
| `Tier`, `TierCriteria` + `/tiers`, `/admin/tiers` | `tier/` |
| `Benefit`, `TierBenefit` + `/membership/benefits`, `/admin/benefits` | `benefit/` |
| `TierUpgradePrice` + `/admin/tier-upgrade-prices` | `pricing/` |
| `Membership` + `/membership/*` (subscribe, change-plan, upgrade, downgrade, cancel) | `membership/` |
| `MembershipAuditLog` | `audit/` |
| `TierCache` (Redis) + `resolveTier()` engine (§4) | `evaluation/` |
| Razorpay order creation + signature verification (§5.1, §5.3) | `payment/` |
| Order Service / Cohort Service reads (Assumption 3) | `client/order/`, `client/cohort/` |
| `GET /internal/benefits/{userId}` (§3.4) | `internal/` |
| Expiry scheduled job (§5.6) | `scheduler/` |

No entity or endpoint from §2/§3 is missing a package. ✅

### 9.4 Conventions used

- **Interface + `impl/` subpackage for services** — enables mocking in unit tests and keeps a clean seam if a service ever needs multiple implementations.
- **DTOs never leak entities across layers** — controllers only see DTOs; `mapper/` (MapStruct) converts between entity and DTO. Keeps persistence models decoupled from API contracts.
- **`common/` holds only truly cross-cutting code** (exceptions, base response envelope, enums shared by 2+ features) — deliberately kept small so it doesn't become a dumping ground.
- **`client/` isolates all outbound calls to other services** behind interfaces, so the rest of the codebase (and tests) never talks to `WebClient` directly — makes it trivial to mock Order/Cohort service responses in tests.
- **`payment/` isolates Razorpay** behind `RazorpayClientAdapter` — if the payment provider ever changes, only this package is touched.
- **Flyway migrations are numbered and one-concern-per-file**, matching the entity list in §2 in the same order, so schema history reads like a changelog of the data model's evolution.
- **Idempotency (NFR)** is handled by a dedicated `PaymentRecord` entity keyed on `razorpayPaymentId`, checked before any Membership/upgrade mutation — see §6.
- **Tests mirror the main package structure 1:1**, plus a `support/` package for shared Testcontainers config and fixture builders.

### 9.5 What's intentionally *not* over-engineered here

- No separate "domain" vs "infrastructure" hexagonal split — for a service this size, feature packages with clean internal layering give most of the same benefit with less ceremony. Worth revisiting only if the service grows significantly or needs to swap persistence technology.
- No CQRS / separate read-models — the lazy-cache pattern in `evaluation/` already handles the one place where read/write separation actually matters (tier resolution).
# FirstClub Membership Program — Requirements

**Status:** v2 — Finalized for Demo Implementation  
**Last updated:** 2026-08-16  
**Scope:** Membership Service / Subscription Plans Engine

---

## 1. Purpose

Build a standalone Membership Service that manages:

- Membership Plans and their pricing options.
- Plan-level benefits and Tier-specific additional benefits.
- User Membership lifecycle.
- Automatic initial Tier evaluation at subscription time.
- Paid Plan upgrades.
- Paid Tier upgrades.
- Membership cancellation and expiry.
- Benefit resolution for downstream services such as Checkout.

Users, Orders, and Cohorts are owned by external services. The Membership Service only reads the required data from those services when assigning the initial Tier.

Payment processing is mocked/assumed successful in v1. No payment gateway, PaymentRecord, or payment-provider idempotency implementation is part of the demo.

---

## 2. Core Domain Model

### 2.1 Plan

A Plan represents the membership product.

A Plan can have multiple pricing options:

- Monthly
- Quarterly
- Yearly

These are **pricing options of the same Plan**, not separate Plans.

Plans have a unique `rank`. A higher rank represents a higher Plan.

Each Plan also has:

```text
consecutive_tier_upgrade_price
```

This is the price for one Tier-rank jump within that Plan.

### 2.2 PlanPrice

`PlanPrice` represents the billing-period-specific price and duration of a Plan.

A Plan may have:

```text
MONTHLY   → 30 days
QUARTERLY → 90 days
YEARLY    → 365 days
```

The exact price is configurable by the admin.

**Requirement:** Benefits/features remain consistent across Monthly, Quarterly, and Yearly prices of the same Plan. Billing period changes the price and duration, not the feature set.

### 2.3 Tier

A Tier is a membership level belonging to exactly one Plan.

Initial supported tiers for a Plan may be:

```text
Silver   → rank 1
Gold     → rank 2
Platinum → rank 3
```

The implementation must keep Tier as a database entity rather than an enum.

Each Tier has a mandatory `plan_id` foreign key. Therefore, the same Tier name may exist under different Plans as different Tier records.

Example:

```text
Premium
├── Silver
├── Gold
└── Platinum

Basic
├── Silver
└── Gold
```

Tier rank represents hierarchy only. It does not determine whether a user qualifies for the Tier.

### Active Tier uniqueness

Within a Plan, active Tier names and active Tier ranks must be unique. Inactive historical Tiers may have duplicate names/ranks.

```text
Premium + Silver + ACTIVE      → allowed
Premium + Silver + INACTIVE    → allowed
Premium + Silver + ACTIVE      → rejected
Basic   + Silver + ACTIVE      → allowed
```

### 2.3.1 Tier Eligibility

Tier eligibility is stored as JSON/JSONB and represented in application code as `Map<String, Object>`.

Example:

```json
{
  "matchMode": "ALL",
  "rules": [
    {
      "type": "MIN_ORDER_COUNT",
      "value": 10
    },
    {
      "type": "MIN_MONTHLY_ORDER_VALUE",
      "value": 50000
    }
  ]
}
```

Eligibility answers whether a user qualifies for a specific Tier.

### 2.4 PlanBenefit

There is **one benefit configuration table: `PlanBenefit`**.

There is no separate global `Benefit` table and no separate `TierBenefit` table in v1.

```text
PlanBenefit
    plan_id       NOT NULL
    tier_id       NULL
    type
    value
    discount_type
    eligibility
    monthly_limit
    is_active
```

Meaning:

```text
tier_id = NULL
    → base/default benefit of the Plan

tier_id = X
    → additional benefit for Tier X under that Plan
```

This allows the same Tier name to have different benefits under different Plans.

Example:

```text
Premium + NULL
    DISCOUNT = 10%

Premium + Gold
    DISCOUNT = +5%

Premium + Platinum
    DISCOUNT = +10%
```

Tier-specific benefits are additive configuration; effective-benefit resolution is owned by `PlanBenefitService`.

### 2.5 Supported Benefit Types

Initial benefit types are:

```text
FREE_DELIVERY
DISCOUNT
EARLY_ACCESS
PRIORITY_SUPPORT
```

`DISCOUNT` supports:

```text
PERCENT
FLAT
```

`value`, `discount_type`, and `eligibility` are stored in the PlanBenefit configuration because the same benefit type may have different values/applicability rules for different Plans or Tiers.

Benefit applicability may target products, categories, items, or global/all applicable products through `eligibility`.

### 2.6 Monthly Benefit Entitlement

Every applicable benefit may have a configured monthly entitlement/limit.

The cap belongs to the `PlanBenefit` configuration.

Example:

```text
Gold adds FREE_DELIVERY
monthly_limit = 3
```

The configured monthly entitlement is returned as benefit metadata to downstream consumers. The Membership Service does **not** track or enforce per-user consumption in v1.

`UserBenefitUsage` is explicitly deferred.

---

## 3. Functional Requirements

### FR-1 — View Plans

The system shall allow clients to retrieve active Plans.

The response shall include:

- Plan identity.
- Plan rank.
- Available PlanPrice options.
- Billing period.
- Duration.
- Price.
- Currency.
- Benefit summary where required.

---

### FR-2 — Manage Plans and Prices

Admins shall be able to:

- Create a Plan.
- Update a Plan.
- Disable a Plan.
- Configure Plan rank.
- Configure `consecutive_tier_upgrade_price`.
- Create Monthly/Quarterly/Yearly PlanPrice records.
- Update/disable PlanPrice records.

The system shall preserve inactive Plans/PlanPrices needed by existing memberships.

---

### FR-3 — Manage Tiers and Plan Benefits

Admins shall be able to:

- Create/update/disable Tiers.
- Create a Tier under a specific Plan.
- Configure Tier rank.
- Configure Tier evaluation criteria.
- Create/update/disable PlanBenefit records.
- Create base Plan benefits using `tier_id = NULL`.
- Create Tier-specific benefits using `tier_id = <tierId>`.

A Tier cannot be moved to another Plan through normal Tier update. Create a separate Tier record under the target Plan instead.

A PlanBenefit must always belong to a Plan.

For a Tier-specific PlanBenefit:

```text
PlanBenefit.plan_id == Tier.plan_id
```

The same Tier name may be configured differently for different Plans.

---

### FR-4 — Resolve Plan and Tier Benefits

The system shall resolve effective benefits from:

```text
Base PlanBenefit
    +
PlanBenefit for current Tier
    =
Effective Benefits
```

The system shall not discard the base Plan benefits when a Tier-specific benefit exists.

The effective-benefit resolution logic is owned by `PlanBenefitService`.

The exact benefit-type-specific aggregation behavior is an application concern and must remain consistent with the supported benefit model; v1 does not require a separate benefit-usage engine.

Monthly entitlements/limits are configuration metadata only in v1.

---

### FR-5 — Provide Benefits to Checkout

The Membership Service shall expose an internal service-to-service API for downstream consumers such as Checkout.

The endpoint shall:

1. Identify the user's active Membership.
2. Read its Plan.
3. Read its stored current Tier.
4. Load base PlanBenefit rows.
5. Load Tier-specific PlanBenefit rows when a Tier exists.
6. Resolve the effective benefits.
7. Return the effective benefits.

If the user has no active Membership, no membership benefits shall be returned.

If:

```text
current_tier_id = NULL
```

the user receives only the base Plan benefits.

The benefits endpoint is internal/service-to-service and is not a public client API.

---

### FR-6 — View Plans and Tiers

Clients/admins shall be able to retrieve:

- Active Plans.
- Available PlanPrice options.
- Active Tiers.
- Tier ranks.
- Tier criteria where appropriate.
- Plan benefit configuration where appropriate.

---

### FR-7 — Subscribe to a Plan

A user shall be able to subscribe to a selected:

```text
Plan + PlanPrice
```

The flow shall:

1. Validate that the user does not already have an active Membership.
2. Validate that the selected Plan is active.
3. Validate that the selected PlanPrice is active and belongs to the selected Plan.
4. Assume payment succeeds for the demo.
5. Evaluate the user's initial Tier.
6. Create the Membership.
7. Set the selected `plan_id`.
8. Set the selected `plan_price_id`.
9. Set `start_date = now`.
10. Set `expiry_date` from the selected PlanPrice duration.
11. Store the resulting Tier on Membership, or `NULL` if no Tier qualifies.

---

### FR-8 — Change Plan

A user may change to a higher-ranked Plan.

The system shall:

1. Validate the target Plan.
2. Validate the target PlanPrice belongs to the target Plan and is active.
3. Assume payment succeeds for the demo.
4. Start the target Plan immediately.
5. Start a fresh duration based on the selected target PlanPrice.
6. Update `plan_id`.
7. Update `plan_price_id`.
8. Update `start_date`.
9. Update `expiry_date`.
10. Re-evaluate the user's Tier against the target Plan.
11. Store the resulting target-Plan Tier, or `NULL` if no Tier qualifies.

The previous Plan's Tier must not remain attached after a Plan change.

Plan downgrade is not supported in v1.

---

### FR-9 — Paid Tier Upgrade

A user may upgrade to a higher-ranked Tier only when the Membership already has a current Tier.

If:

```text
current_tier_id = NULL
```

the user cannot directly purchase a Tier upgrade in v1.

The system shall validate:

```text
currentTier != NULL
targetTier.active == true
targetTier.plan_id == membership.plan_id
targetTier.rank > currentTier.rank
```

The upgrade price shall be calculated using the Plan's common consecutive Tier upgrade price:

```text
rankDifference =
    targetTier.rank - currentTier.rank

upgradePrice =
    rankDifference
    × plan.consecutive_tier_upgrade_price
```

Example:

```text
Silver    = rank 1
Gold      = rank 2
Platinum  = rank 3

Plan.consecutive_tier_upgrade_price = ₹500

Silver → Gold
= 1 × ₹500
= ₹500

Gold → Platinum
= 1 × ₹500
= ₹500

Silver → Platinum
= 2 × ₹500
= ₹1,000
```

The system shall:

1. Validate the target Tier belongs to the Membership's current Plan.
2. Assume payment succeeds for the demo.
3. Update `current_tier_id`.
4. Set `tier_source = PAID_UPGRADE`.
5. Keep the existing Plan unchanged.
6. Keep the existing PlanPrice unchanged.
7. Keep `start_date` and `expiry_date` unchanged.

There is no separate TierUpgradePrice table in v1.

Voluntary Tier downgrade is not supported.

---

### FR-10 — Demo Payment

Payment is intentionally simplified for v1.

The system shall:

- Treat the demo payment step as successful.
- Continue the Membership business operation after the assumed successful payment.

The following are out of scope:

- Real payment gateway integration.
- Payment provider IDs.
- Payment webhooks.
- Payment signature verification.
- Refund processing.
- Payment reconciliation.
- Payment-provider idempotency.
- A separate `PaymentRecord` entity.

---

### FR-11 — Initial Tier Assignment

Initial Tier evaluation happens **only at subscription time in v1**.

The system shall read required user information from external services, such as:

- Order count.
- Order value.
- Cohort/tags.

It shall evaluate active Tiers belonging to the selected Plan and select the highest qualifying Tier.

If no Tier criteria are satisfied:

```text
current_tier_id = NULL
```

The user then receives only the base Plan benefits.

There is no scheduled or automatic Tier re-evaluation in v1.

Future dynamic Tier evaluation is deferred.

---

### FR-12 — Cancel Membership

A user shall be able to cancel an active Membership.

Cancellation shall:

- Mark the Membership as `CANCELLED`.
- Not issue a refund.
- Not modify historical Membership state other than the cancellation status/update.

No downgrade, refund, or audit-log subsystem is introduced as part of cancellation in v1.

---

### FR-13 — Track Current Membership

The system shall allow the user to retrieve their current Membership, including:

- Membership ID.
- Plan.
- PlanPrice.
- Current Tier, if any.
- Tier source.
- Status.
- Start date.
- Expiry date.

---

### FR-14 — Membership Expiry

The system shall support Membership expiry.

When:

```text
expiry_date <= now
```

an active Membership becomes:

```text
EXPIRED
```

Expired memberships must not provide membership benefits.

A user may subscribe again after expiry.

Before an active-membership check, stale active records may be transitioned to `EXPIRED` when their expiry date has already passed.

---

## 4. Non-Functional Requirements

### NFR-1 — Safe Membership Mutations

Membership state mutations shall execute transactionally.

Concurrent mutations must not silently overwrite each other.

Payment-provider idempotency is out of scope because payment is mocked.

### NFR-2 — Data Integrity

The system shall enforce:

- At most one active Membership per user.
- Unique Plan ranks.
- Valid PlanPrice → Plan relationship.
- Active Tier name unique within a Plan.
- Active Tier rank unique within a Plan.
- Inactive Tier duplicates are allowed.
- Valid PlanBenefit → Plan relationship.
- Valid PlanBenefit → Tier relationship when `tier_id` is non-null.
- `PlanBenefit.plan_id` must equal `Tier.plan_id` when `tier_id` is non-null.
- Membership.current_tier must belong to Membership.plan.
- Unique base benefit per `(plan_id, type)`.
- Unique Tier benefit per `(plan_id, tier_id, type)`.
- Tier upgrade target must have a higher rank and belong to the same Plan.
- Plan change target must be a valid active Plan.
- Selected PlanPrice must belong to the selected Plan.

### NFR-3 — Concurrency

Optimistic locking is required for Membership state mutations.

The `Membership` entity uses a version field with JPA optimistic locking:

```java
@Version
private Long version;
```

Concurrent Membership mutations must not silently overwrite each other.

A stale Membership update must result in a conflict rather than silently replacing a newer state.

Optimistic locking does not by itself enforce the one-active-membership-per-user invariant during concurrent inserts; that invariant should also be protected at the database level for production hardening.

### NFR-4 — Error Handling

The service shall provide consistent REST error responses through the common exception handling layer.

Examples:

- Invalid Plan → `404`
- Invalid Tier → `404`
- Invalid PlanPrice → `404`
- Invalid Benefit configuration → `400`
- Unsupported downgrade → `409`
- Duplicate active subscription → `409`
- Cross-Plan Tier upgrade → `409`
- Concurrent Membership mutation → `409`

Real payment verification errors are out of scope because payment is mocked.

### NFR-5 — Extensibility

The model shall allow:

- New Plans without schema changes.
- New PlanPrice billing periods where supported by the domain.
- New Tiers without schema changes.
- Future benefit types through application-level validation/behavior.
- Future per-user benefit usage tracking without changing the meaning of current PlanBenefit configuration.
- Future real payment integration without coupling the current Membership domain model to a payment provider.

---

## 5. Explicitly Out of Scope for v1

The following are intentionally not implemented:

- Separate global `Benefit` entity/table.
- Separate `TierBenefit` entity/table.
- Separate `TierUpgradePrice` entity/table.
- `UserBenefitUsage` / per-user benefit consumption tracking.
- Dynamic Tier re-evaluation after every order.
- Scheduled/background Tier re-evaluation.
- Voluntary Plan downgrade.
- Voluntary Tier downgrade.
- Refund processing.
- Real payment gateway/payment processing.
- Payment provider webhooks.
- Payment signature verification.
- Payment reconciliation.
- Payment-provider idempotency.
- Separate `PaymentRecord` entity.
- `MembershipAuditLog` entity/subsystem.
- Invoice/settlement/accounting system.
- Ownership of Users, Orders, or Cohorts.
- Building real Order/Cohort/Checkout services; they are external dependencies/mocks for the Membership Service demo.

---

## 6. API Requirements

### Public / Client APIs

```text
GET  /api/v1/plans
GET  /api/v1/plans/{planId}/tiers

GET  /api/v1/memberships/active?userId={userId}
POST /api/v1/memberships

PUT  /api/v1/memberships/{membershipId}/plan
POST /api/v1/memberships/{membershipId}/tier-upgrade

GET  /api/v1/memberships/{membershipId}/benefits

POST /api/v1/memberships/{membershipId}/cancel
```

### Admin APIs

```text
POST   /api/v1/admin/plans
PUT    /api/v1/admin/plans/{planId}
DELETE /api/v1/admin/plans/{planId}

POST   /api/v1/admin/plans/{planId}/prices
PUT    /api/v1/admin/plans/{planId}/prices/{priceId}
DELETE /api/v1/admin/plans/{planId}/prices/{priceId}

GET    /api/v1/admin/plans/{planId}/benefits
POST   /api/v1/admin/plans/{planId}/benefits
PUT    /api/v1/admin/plans/{planId}/benefits/{benefitId}

GET    /api/v1/admin/tiers
POST   /api/v1/admin/tiers
PUT    /api/v1/admin/tiers/{tierId}
```

Tier creation requires a Plan:

```json
{
  "planId": "PLAN_UUID",
  "name": "Gold",
  "rank": 2,
  "eligibility": {}
}
```

There is no normal API for moving a Tier between Plans.

### Internal API

```text
GET /api/v1/internal/benefits/{userId}
```

This endpoint is service-to-service only and must not be exposed as a public/client-facing endpoint.

---

## 7. Key Business Rules

### Rule 1 — Billing Period

```text
Plan
 ├── Monthly PlanPrice
 ├── Quarterly PlanPrice
 └── Yearly PlanPrice
```

Benefits remain consistent across all three.

### Rule 2 — Base Benefit

```text
PlanBenefit(plan_id = P, tier_id = NULL)
```

is the Plan's base benefit.

### Rule 3 — Tier Ownership

```text
Tier.plan_id = P
```

means Tier `T` belongs to Plan `P`.

A Tier from another Plan must never be attached to the Membership or its benefits.

### Rule 4 — Tier Benefit

```text
PlanBenefit(plan_id = P, tier_id = T)
```

is an additional benefit for Tier `T` under Plan `P`.

```text
PlanBenefit.plan_id == Tier.plan_id
```

must hold.

### Rule 5 — No Tier

```text
current_tier_id = NULL
```

means the user has no qualified Tier and receives base Plan benefits only.

### Rule 6 — Effective Benefits

```text
effective =
    base PlanBenefit
    +
    current Tier PlanBenefit
```

### Rule 7 — Plan Change

A Plan change starts the target Plan immediately and uses a fresh duration based on the selected target PlanPrice.

The user's Tier is re-evaluated against the target Plan.

### Rule 8 — Tier Upgrade

Tier upgrades do not reset or extend the subscription period.

Only the stored Tier changes after successful payment.

### Rule 9 — Tier Upgrade Price

```text
(targetRank - currentRank)
× Plan.consecutive_tier_upgrade_price
```

### Rule 10 — No Downgrades

The service supports upgrades only:

```text
Plan:  higher rank only
Tier:  higher rank only
```

---

## 8. Requirements Traceability

| Requirement | Main implementation area |
|---|---|
| FR-1 / FR-2 — Plans & PlanPrice | Plan + PlanPrice |
| FR-3 — Tier/Benefit administration | Tier + PlanBenefit |
| FR-4 — Benefit resolution | PlanBenefitService |
| FR-5 — Checkout benefit lookup | Internal benefit API / PlanBenefitService |
| FR-6 — Plans/Tiers discovery | Plan/Tier APIs |
| FR-7 — Subscribe | MembershipService |
| FR-8 — Plan change | MembershipService |
| FR-9 — Tier upgrade | MembershipService |
| FR-10 — Demo payment | Mock/assumed-success payment step |
| FR-11 — Initial Tier assignment | TierAssignmentService + TierEligibilityEvaluator |
| FR-12 — Cancellation | MembershipService |
| FR-13 — Current Membership | Membership API |
| FR-14 — Expiry | MembershipService |
| NFR-1 — Safe Membership Mutations | Membership transaction boundaries |
| NFR-2 — Data integrity | DB constraints + validators |
| NFR-3 — Concurrency | Membership optimistic locking |
| NFR-4 — Error handling | Global exception handling |
| NFR-5 — Extensibility | Table-driven Plans/Tiers/PlanBenefits |

---

## 9. Final v2 Decision

The v1 Membership Service uses the following simplified model:

```text
Plan
 ├── PlanPrice (Monthly / Quarterly / Yearly)
 ├── Tier
 │    ├── Silver
 │    ├── Gold
 │    └── Platinum
 ├── PlanBenefit (tier_id = NULL)
 ├── PlanBenefit (tier_id = Plan Tier)
 └── consecutive_tier_upgrade_price

Membership
 ├── Plan
 ├── PlanPrice
 └── current Tier (nullable)

Payment
 └── assumed successful / mocked in v1
```

This document is the requirements baseline for the v2 demo implementation and is approved for implementation.

# FirstClub Membership Program — Requirements Document

**Status:** v1 — Finalized
**Owner:** TBD
**Last updated:** 2026-08-13

---

## 1. Overview

FirstClub will offer a subscription-based Membership Program. Users pay for a **Plan**
(Monthly / Quarterly / Yearly) to become a member. Once a member, the user is placed into
a **Tier** (Silver / Gold / Platinum) that determines the benefits they receive. Tier is
primarily earned through shopping behavior, but a member can also pay to upgrade their
tier directly. Benefits (discounts, free delivery, early access, priority support) are
configurable per tier by the business, not hardcoded.

---

## 2. Glossary

| Term | Meaning |
|---|---|
| **Plan** | The billing subscription a user pays for — Monthly, Quarterly, or Yearly. Defines price and validity period. |
| **Membership** | An active instance of a user having subscribed to a Plan. Has a start date and expiry date. |
| **Tier** | A benefit level (Silver / Gold / Platinum) attached to a user's Membership. Determines which benefits apply. |
| **Tier Criteria** | Rules (order count, order value, cohort) used to auto-compute a user's Tier. |
| **Benefit** | A perk (e.g. free delivery, X% discount, early access, priority support) attached to a Tier. |
| **Cohort** | A named user segment (e.g. "New User", "Influencer", "Corporate") that can influence Tier assignment, defined/managed outside this system. |

---

## 3. Assumptions & Design Decisions

These points were not fully specified in the raw requirements and were clarified during review:

1. **Tier evaluation frequency:** Tier is computed **lazily and cached**, not via a batch job. Details in section 4.3, FR-11. TTL is **fixed at 1 hour** for now (not admin-configurable at this stage) — to be revisited later.
2. **Benefit config structure:** Benefits follow a **structured, admin-configurable schema** — not a fixed enum baked into code. Each Tier has a list of Benefits; each Benefit is defined by a schema of `{type, value, scope, active}` so admins can add/edit/disable benefit instances without a deployment. We start **minimal** with a small set of supported `type`s at launch and the schema is designed to allow new `type`s to be added later without breaking existing config. Initial minimal set: FREE_DELIVERY, DISCOUNT_PERCENT, EARLY_ACCESS, PRIORITY_SUPPORT (flag only). Managed via an admin-only config API.
3. **Cohort source:** Cohort membership is provided by an external/existing user-segmentation system; this service only reads a `cohort_id`/`cohort_tags` field on the user, it does not compute cohorts itself.
4. **Paid tier upgrade pricing:** The price to upgrade a tier is configurable per **(Plan, source Tier → target Tier)** combination — not a single global fee per tier-pair. E.g. Silver→Gold can cost differently for a Monthly member vs. a Yearly member, and Silver→Platinum is its own configurable price point distinct from doing Silver→Gold→Platinum in two steps. Admin manages this as a price matrix/table, editable without code changes.
5. **Paid tier upgrade validity:** A paid upgrade lasts until the current Membership (Plan) expires or until the user is voluntarily/automatically downgraded — it is not a separate subscription.
6. **Manual downgrade:** A user can voluntarily downgrade their Tier (e.g. Gold → Silver) at will; this takes effect immediately and there is no refund for any amount previously paid for the paid upgrade.
7. **One active Membership per user:** A user can have only one active Plan at a time (no stacking Monthly + Yearly).
8. **Re-subscription:** Since there's no auto-renew, once a Membership expires the user drops to "no active membership" (no default free tier) until they subscribe again.
9. **Payment integration:** Razorpay (test mode) is the payment gateway for both Plan purchase and paid Tier upgrade; detailed billing/invoicing requirements are intentionally light in this doc and will be expanded when that work starts, per your note that it's a "later part."

---

## 4. Functional Requirements

Each requirement includes a user story and testable acceptance criteria.

### 4.1 Membership Plans

**FR-1: View available Plans**
> As a user, I want to see all available membership plans with their pricing, so that I can choose the one that fits my needs.

- Acceptance Criteria:
  - Given a user opens the Membership page, when the plans load, then Monthly, Quarterly, and Yearly plans are shown with price, billing duration, and a summary of tier benefits.
  - Plan pricing is configurable by admins without a code deployment.
  - Inactive/disabled plans are not shown to users.

**FR-2: Configurable Plans (Admin)**
> As an admin, I want to create/edit/disable membership plans, so that business teams can change pricing or introduce new plans without engineering involvement.

- Acceptance Criteria:
  - Admin can set: plan name, duration (days), price, currency, active/inactive status.
  - Disabling a plan does not affect existing active subscribers on that plan.

---

### 4.2 Membership Benefits (Configurable per Tier)

**FR-3: Configure Tier Benefits (Admin)**
> As an admin, I want to configure which benefits apply to each tier using a structured, extensible schema, so that I can change perks (discount %, free delivery, early access, priority support) without code changes, and add new benefit types later without breaking existing configuration.

- Acceptance Criteria:
  - Each Tier (Silver/Gold/Platinum) has an independently configurable list of benefits, each defined by a structured schema: `{type, value, scope (optional), active}`.
  - **Launch scope is minimal** — supported benefit `type`s at launch: Free delivery (with min order value threshold), Extra discount % (global or scoped to specific categories/items), Early access to sales, Priority support (flag only). The schema is designed so additional `type`s can be introduced later purely via configuration/data, without requiring a new code path for every type.
  - Higher tiers can be configured with cumulative or entirely distinct benefits — the system does not assume higher = strictly more, it just applies whatever is configured.
  - Changes to benefit configuration apply to all members of that tier from that point forward (not retroactively to past orders).

**FR-4: View my benefits**
> As a member, I want to see what benefits my current tier unlocks, so that I understand the value I'm getting.

- Acceptance Criteria:
  - Given an active member, when they view "My Membership," then their current Tier and its full benefit list are displayed clearly.

**FR-5: Apply benefits at checkout**
> As a member, I want my tier's discount and free delivery benefits to automatically apply at checkout, so that I don't have to do anything extra to redeem them.

- Acceptance Criteria:
  - Given a member with an active membership and a valid benefit (e.g. free delivery over ₹X, or Y% off eligible categories), when they reach checkout, then the discount/free-delivery is auto-applied to eligible items.
  - Given a non-member or a member with an expired plan, when they checkout, then no membership benefits are applied.
  - Benefit scoping (category/item eligibility) is respected — ineligible items are excluded from the discount.

---

### 4.3 User Actions — Subscription Lifecycle

**FR-6: Get available Plans & Tiers info**
> As a user, I want to fetch the list of plans and see what each tier offers, so that I can make an informed decision before subscribing.

- Acceptance Criteria:
  - API/UI returns all active Plans and all Tiers with their current benefit configuration.

**FR-7: Subscribe to a Plan**
> As a user, I want to subscribe to a plan by paying for it, so that I become a member and start receiving tier benefits.

- Acceptance Criteria:
  - Given a user with no active membership, when they select a Plan and complete payment (Razorpay), then a Membership record is created with `startDate = now`, `expiryDate = now + plan duration`, and status = ACTIVE.
  - The user's initial Tier is computed immediately at subscribe time based on existing order history, order value, and cohort (per Tier Criteria — see FR-11).
  - Given a user who already has an active membership, when they try to subscribe again, then the system rejects the request and instead offers Plan change (see FR-8) — no duplicate/stacked memberships.
  - On payment failure, no Membership is created and the user is informed.

**FR-8: Change Plan**
> As a member, I want to switch my billing plan (e.g. Monthly → Yearly), so that I can pick a cheaper/longer cycle.

- Acceptance Criteria:
  - Given an active member, when they select a different Plan and complete any required payment adjustment, then their Membership's plan and expiry are updated accordingly.
  - Changing Plan does not reset or change the user's current Tier.

**FR-9: Paid Tier Upgrade**
> As a member, I want to pay to upgrade my tier directly (e.g. Silver → Gold), so that I can access higher-tier benefits immediately without waiting to meet the usage criteria.

- Acceptance Criteria:
  - Upgrade pricing is configurable as a **matrix keyed by (Plan, source Tier → target Tier)** — e.g. (Monthly, Silver→Gold), (Yearly, Silver→Gold), (Monthly, Gold→Platinum), (Yearly, Silver→Platinum) can each have distinct, independently configurable prices. Admins manage this matrix without a code deployment; any (Plan, tier-pair) combination not explicitly configured is treated as unavailable for direct paid upgrade.
  - Given an active member below the top tier, when they choose "Upgrade Tier" to a specific target tier and complete payment for the price configured for their (current Plan, current Tier → target Tier), then their Tier is updated immediately to the selected higher tier.
  - The paid-upgrade tier remains in effect until the Membership expires, is cancelled, or the user manually downgrades (FR-10).
  - A user cannot "upgrade" to a tier they already have or a lower tier via this flow.
  - The tier cache (FR-11) is **invalidated and refreshed immediately** on a successful paid upgrade — this does not wait for TTL expiry.

**FR-10: Voluntary Tier Downgrade**
> As a member, I want to manually downgrade my tier, so that I can opt out of a higher tier if I no longer want it.

- Acceptance Criteria:
  - Given an active member on Tier X, when they choose to downgrade to a lower Tier Y, then their Tier updates to Y immediately and Y's benefits apply from that point on.
  - No refund is issued for any previously paid tier-upgrade fee.
  - This action is user-initiated only; it is independent of automatic tier evaluation.
  - The tier cache (FR-11) is **invalidated and refreshed immediately** on a voluntary downgrade — this does not wait for TTL expiry.

**FR-11: Automatic Tier Assignment (Lazy, Cached Evaluation)**
> As a member, I want my tier to automatically go up as I shop more, so that I'm rewarded for my loyalty without having to do anything manually.

- Design model (confirmed): Tier is **not** computed by a recurring batch job over all members. Instead:
  - Tier is computed at the moment it's **needed** (e.g. at Plan selection/subscribe time, and any subsequent read of "current tier") and the result is **cached** as `{user_id → tier, computed_at}` with a **fixed TTL of 1 hour** (hardcoded for now, not admin-configurable — may be revisited later).
  - On a cache hit within TTL, the cached tier is used — no recomputation.
  - On a cache miss or expired TTL, tier is recomputed fresh from current order count/value/cohort against Tier Criteria, and the cache is refreshed.
- Acceptance Criteria:
  - Tier Criteria are configurable per tier and can include: minimum number of orders (in a period), minimum total order value (in a period, e.g. monthly), and/or cohort membership.
  - Given an active member whose cached tier has expired, when their tier is next read (checkout, "My Membership," etc.) and they now meet a higher tier's criteria, then their Tier is upgraded and re-cached at that point.
  - Automatic (lazy) evaluation never downgrades a user below a tier they reached via a **paid** upgrade (FR-9) — only voluntary downgrade (FR-10) or plan expiry can reduce a paid-upgraded tier. Lazy evaluation only moves a user up, or up further, based on criteria.
  - Tier Criteria are configurable by admins without code changes.
  - **Known trade-off:** Since evaluation is lazy, a user may not be *upgraded* until something actually triggers a read after their cache expires — there is no guarantee of immediate real-time detection the moment they cross a threshold. This is acceptable per product decision. Visibility is handled by FR-13 (member can always pull their latest tier on demand).

**FR-12: Cancel Membership**
> As a member, I want to cancel my membership, so that I stop being charged and am no longer a member.

- Acceptance Criteria:
  - Given an active member, when they cancel, then their Membership status becomes CANCELLED and benefits stop applying **immediately** (per your confirmation — no benefits-until-expiry grace period).
  - **No refund is issued or processed in any case.** Cancellation is purely a status change — it stops future benefit access, nothing more. No refund calculation, no partial-period proration, no refund API call to Razorpay.
  - A cancelled user can subscribe again later as a new Membership (Tier is recomputed fresh at that point per FR-7).

**FR-13: Track current Membership & expiry**
> As a member, I want to see my current plan, tier, and expiry date, so that I know when I need to renew.

- Acceptance Criteria:
  - Given an active member, when they view "My Membership," then Plan name, Tier, start date, expiry date, and days remaining are shown.
  - This read triggers the standard lazy tier-cache logic (FR-11) — if the cache is expired, tier is recomputed fresh at this point, so the user always sees an up-to-date tier here.
  - Given an expired membership, when the user views their status, then it clearly shows "Expired — no auto-renew" with a prompt to resubscribe.

**FR-14: No auto-renewal / expiry**
> As a member, I want my membership to simply expire at the end of my billing cycle if I don't renew, so that I'm never charged without my explicit action.

- Acceptance Criteria:
  - Given a Membership reaches its `expiryDate`, when the scheduled expiry job runs, then status becomes EXPIRED and Tier benefits stop applying.
  - No automatic charge is attempted at cycle end.

---

## 5. Non-Functional Requirements

| Category | Requirement |
|---|---|
| **Consistency** | Paid upgrade / voluntary downgrade must invalidate the tier cache immediately (no TTL wait). Auto-earned tier changes are eventually-consistent within the 1-hour TTL window — this is an accepted trade-off, not a bug. |
| **Cache** | Tier cache TTL is **fixed at 1 hour** for now (not admin-configurable at this stage); may be made configurable in a later iteration if needed. |
| **Auditability** | All Tier changes (auto, paid-upgrade, voluntary downgrade) and Plan changes must be logged with timestamp, reason/trigger, and before/after state. |
| **Idempotency** | Payment webhooks (Razorpay) must be handled idempotently to avoid duplicate Membership creation or double tier-upgrades on retry. |
| **Configurability** | Plans, Tiers, Tier Criteria, and Benefits must all be configurable via admin tooling without a code deployment. |
| **Scalability** | Since tier evaluation is lazy/on-demand rather than batch, the recompute-and-cache path must be fast enough to run inline on a read (checkout, membership view) without noticeable latency — see Availability row below. |
| **Security** | Payment flows must never expose Razorpay secrets client-side; all tier/benefit mutations require authenticated admin access. |
| **Availability** | Checkout-time benefit application is on the critical path — target 99.9% availability, low-latency (<200ms) lookup of a user's active benefits. |

---

## 6. Out of Scope (for this document)

- Detailed Razorpay integration flow, refund policy, invoicing/GST — to be covered in a separate Billing requirements doc when that work begins.
- Cohort computation logic (assumed to be owned by an existing/external segmentation system).
- Customer support tooling for priority support (only the entitlement flag is in scope here).
- Push/email notification content and delivery infrastructure — **including expiry reminders and any other proactive notifications** — is entirely out of scope for this phase. All membership/tier status is available via pull (FR-4, FR-13) only.

---

## 7. Decisions Log

Key decisions made during requirements review, for traceability:

1. **Tier evaluation model:** Lazy, cache-based evaluation with a fixed 1-hour TTL (see FR-11) — not a scheduled batch job.
2. **Benefit types at launch:** Structured, extensible schema; launch set is minimal (Free delivery, Discount %, Early access, Priority support flag) and admin-configurable — see Assumption 2 and FR-3.
3. **Tier-upgrade notifications:** Not needed for now — covered sufficiently by FR-13's pull-based tier visibility. No separate push-notification requirement.
4. **Refund policy on cancellation:** No refund in any case. Cancel is a pure status change — see FR-12.
5. **Paid tier-upgrade pricing model:** Configurable as a matrix keyed by (Plan, source Tier → target Tier) — see Assumption 4 and FR-9.
6. **Tier-cache TTL:** Fixed at 1 hour for now (not admin-configurable); may be revisited later.
7. **Notifications (all types):** Not in scope for this phase — expiry reminders, tier-change alerts, etc. are all deferred. All status is available on-demand via pull (FR-4, FR-13).

---

## 8. User Story Summary (Traceability)

| ID | User Story | Status |
|---|---|---|
| FR-1 | View available plans | Approved |
| FR-2 | Admin configures plans | Approved |
| FR-3 | Admin configures tier benefits | Approved |
| FR-4 | View my benefits | Approved |
| FR-5 | Benefits auto-apply at checkout | Approved |
| FR-6 | Get plans & tiers info | Approved |
| FR-7 | Subscribe to a plan | Approved |
| FR-8 | Change plan | Approved |
| FR-9 | Paid tier upgrade | Approved |
| FR-10 | Voluntary tier downgrade | Approved |
| FR-11 | Automatic tier assignment (lazy, cached) | Approved |
| FR-12 | Cancel membership | Approved |
| FR-13 | Track membership & expiry | Approved |
| FR-14 | No auto-renewal / expiry | Approved |
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
- Minimal payment-state representation and idempotency.

Users, Orders, and Cohorts are owned by external services. The Membership Service only reads the required data from those services when assigning the initial Tier.

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

Tier benefits are **additive** to the base Plan benefits.

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

`value` and `eligibility` are stored in the PlanBenefit configuration because the same benefit type may have different values/applicability rules for different Plans or Tiers.

Benefit applicability may target products, categories, items, or global/all applicable products through `eligibility`.

`EARLY_ACCESS` and `PRIORITY_SUPPORT` are boolean benefits.

`FREE_DELIVERY` is rule/eligibility based and does not require a numeric discount value.

### 2.6 Monthly Benefit Entitlement

Every applicable benefit may have a configured monthly entitlement/limit.

The cap belongs to the `PlanBenefit` configuration.

Example:

```text
Gold adds FREE_DELIVERY
monthly_limit = 3
```

Tier benefits add to the base entitlement:

```text
Base Plan = 15/month
Gold      = +3/month

Effective = 18/month
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

A Tier cannot be moved to another Plan through normal Tier update. Create a separate Tier record under the target Plan instead.
- Create/update/disable PlanBenefit records.
- Create base Plan benefits using `tier_id = NULL`.
- Create Tier-specific benefits using `tier_id = <tierId>`.

A PlanBenefit must always belong to a Plan.

The same Tier may be configured differently for different Plans.

---

### FR-4 — Resolve Plan and Tier Benefits

The system shall resolve effective benefits as:

```text
Base PlanBenefit
    +
PlanBenefit for current Tier
    =
Effective Benefits
```

The system shall not replace base benefits when a Tier benefit exists.

Aggregation rules:

- `PERCENT + PERCENT` → add percentages.
- `FLAT + FLAT` → add flat amounts.
- `PERCENT + FLAT` for the same effective discount → invalid.
- Boolean benefits → logical OR.
- Free-delivery/rule-based benefits → combine configured rules according to their scope.

Monthly entitlements/limits are additive when the benefit is additive.

---

### FR-5 — Provide Benefits to Checkout

The Membership Service shall expose an internal service-to-service API for downstream consumers such as Checkout.

The endpoint shall:

1. Identify the user's active Membership.
2. Read its Plan.
3. Read its stored current Tier.
4. Load base PlanBenefit rows.
5. Load Tier-specific PlanBenefit rows when a Tier exists.
6. Aggregate them.
7. Return the effective benefits.

If the user has no active Membership, no membership benefits shall be returned.

If:

```text
current_tier_id = NULL
```

the user receives only the base Plan benefits.

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

1. Validate that the user does not already have a non-expired active Membership.
2. Assume payment succeeds for the demo.
3. Create the Membership.
5. Set the selected `plan_id`.
6. Set the selected `plan_price_id`.
7. Set `start_date = now`.
8. Set `expiry_date` from the selected PlanPrice duration.
9. Evaluate the user's initial Tier.
10. Store the resulting Tier on Membership.

The Membership is created only after the demo payment step succeeds.

---

### FR-8 — Upgrade Plan

A user may upgrade to a higher-ranked Plan.

The system shall:

1. Validate:
   ```text
   targetPlan.rank > currentPlan.rank
   ```
2. Calculate the unused monetary value of the current Membership using:
   ```text
   remainingDays = expiryDate - now

   dailyRate =
       currentPlanPrice.amount /
       currentPlanPrice.durationDays

   unusedCredit =
       dailyRate × remainingDays

   amountPayable =
       max(0, newPlanPrice.amount - unusedCredit)
   ```
3. Use `unusedCredit` as monetary credit against the new PlanPrice.
4. Charge `amountPayable` immediately.
5. Start the new Plan immediately after successful payment.
6. Start a **fresh duration** based on the selected new PlanPrice.
7. Update `plan_id`.
8. Update `plan_price_id`.
9. Update `start_date`.
10. Update `expiry_date`.

The unused value is monetary credit only; it does not extend the new subscription duration.

Plan downgrade is not supported.

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
targetTier.rank > currentTier.rank
```

The upgrade price shall be calculated using the Plan's common consecutive Tier upgrade price:

```text
rankDifference =
    targetTier.rank - currentTier.rank

upgradePrice =
    (targetTier.rank - currentTier.rank)
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

1. Assume payment succeeds for the demo.
2. Update `current_tier_id`.
4. Set `tier_source = PAID_UPGRADE`.
5. Keep the existing Plan unchanged.
6. Keep the existing PlanPrice unchanged.
7. Keep `start_date` and `expiry_date` unchanged.

There is no separate TierUpgradePrice table in v1.

Voluntary Tier downgrade is not supported.

---

### FR-10 — Demo Payment State

If `PaymentRecord` is retained, it represents only demo payment state and Membership-action linkage.

Real payment processing, provider-specific IDs, and a dedicated Payment Service are out of scope for the demo. Payment is assumed successful.

---

### FR-11 — Initial Tier Assignment

Initial Tier evaluation happens **only at subscription time in v1**.

The system shall read required user information from external services, such as:

- Order count.
- Order value.
- Cohort/tags.

It shall evaluate active Tiers in descending rank order and select the highest qualifying Tier.

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
- Create an audit entry.
- Not issue a refund.
- Not modify historical payment records.

No downgrade or refund flow is introduced as part of cancellation.

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

Duplicate Membership mutation requests must not silently corrupt Membership state. Real provider payment-confirmation idempotency is out of scope because payment is mocked.

### NFR-2 — Auditability

The system shall maintain an append-only MembershipAuditLog for important lifecycle changes:

```text
SUBSCRIBED
PLAN_CHANGED
TIER_PAID_UPGRADED
CANCELLED
EXPIRED
```

The audit record shall capture before/after state where applicable.

### NFR-3 — Data Integrity

The system shall enforce:

- At most one active Membership per user.
- Unique Plan ranks.
- Valid PlanPrice → Plan relationship.
- Active Tier name unique within a Plan.
- Active Tier rank unique within a Plan.
- Inactive Tier duplicates are allowed.
- Valid PlanBenefit → Plan relationship.
- Valid PlanBenefit → Tier relationship when `tier_id` is non-null.
- PlanBenefit.plan_id must equal Tier.plan_id when `tier_id` is non-null.
- Membership.current_tier must belong to Membership.plan.
- Unique base benefit per `(plan_id, type)`.
- Unique Tier benefit per `(plan_id, tier_id, type)`.
- Tier upgrade target must have a higher rank and belong to the same Plan.
- Plan upgrade target must have a higher rank.

### NFR-4 — Concurrency

Concurrent Membership mutations must not silently overwrite each other.

Optimistic locking/versioning shall be used on Membership state changes.

### NFR-5 — Error Handling

The service shall provide consistent REST error responses through the common exception handling layer.

Examples:

- Invalid Plan → `404`
- Invalid Tier → `404`
- Invalid Benefit configuration → `400`
- Unsupported downgrade → `400`
- Duplicate active subscription → `409`
- Payment verification failure → `402`
- Concurrent Membership mutation → `409`

### NFR-6 — Extensibility

The model shall allow:

- New Plans without schema changes.
- New PlanPrice billing periods where supported by the domain.
- New Tiers without schema changes.
- Future benefit types through application-level validation/behavior.
- Future per-user benefit usage tracking without changing the meaning of current PlanBenefit configuration.

---

## 5. Explicitly Out of Scope for v1

The following are intentionally not implemented:

- Separate global `Benefit` entity/table.
- Separate `TierBenefit` entity/table.
- Separate `TierUpgradePrice` entity/table.
- `UserBenefitUsage` / per-user benefit consumption tracking.
- Dynamic Tier re-evaluation after every order.
- Voluntary Plan downgrade.
- Voluntary Tier downgrade.
- Refund processing.
- Real payment gateway/payment processing.
- Invoice/settlement/accounting system.
- Ownership of Users, Orders, or Cohorts.
- Building real Order/Cohort/Checkout services; they are external dependencies/mocks for the Membership Service demo.

---

## 6. API Requirements

### Public / Client APIs

```text
GET    /api/v1/plans
GET    /api/v1/plans/{planId}/tiers
GET    /api/v1/membership
POST   /api/v1/membership/subscribe
POST   /api/v1/membership/change-plan
POST   /api/v1/membership/upgrade-tier
POST   /api/v1/membership/cancel
```

### Admin APIs

```text
POST   /api/v1/admin/plans
PATCH  /api/v1/admin/plans/{planId}
DELETE /api/v1/admin/plans/{planId}

POST   /api/v1/admin/plans/{planId}/prices
PATCH  /api/v1/admin/plans/{planId}/prices/{priceId}
DELETE /api/v1/admin/plans/{planId}/prices/{priceId}

POST   /api/v1/admin/plans/{planId}/benefits
PATCH  /api/v1/admin/plans/{planId}/benefits/{benefitId}
DELETE /api/v1/admin/plans/{planId}/benefits/{benefitId}

POST   /api/v1/admin/tiers
PATCH  /api/v1/admin/tiers/{tierId}
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

is always the Plan's base benefit.

### Rule 3 — Tier Ownership

```text
Tier.plan_id = P
```

means Tier `T` belongs to Plan `P`. A Tier from another Plan must never be attached to the Membership or its benefits.

### Rule 4 — Tier Benefit

```text
PlanBenefit(plan_id = P, tier_id = T)
```

is an additional benefit for Tier `T` under Plan `P`.

`PlanBenefit.plan_id` must equal `Tier.plan_id`.

### Rule 5 — No Tier

```text
current_tier_id = NULL
```

means the user has no qualified Tier and receives base Plan benefits only.

### Rule 6 — Tier Aggregation

```text
effective =
    base PlanBenefit
    +
    current Tier PlanBenefit
```

### Rule 7 — Plan Upgrade

A Plan upgrade starts a new billing period immediately.

Unused value from the old subscription becomes monetary credit.

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
| FR-4 — Benefit resolution | PlanBenefit aggregation |
| FR-5 — Checkout benefit lookup | Internal benefit API |
| FR-6 — Plans/Tiers discovery | Plan/Tier APIs |
| FR-7 — Subscribe | Membership lifecycle |
| FR-8 — Plan upgrade | Membership change-plan flow |
| FR-9 — Tier upgrade | Membership upgrade-tier flow |
| FR-10 — Demo payment state | PaymentRecord (optional demo state) |
| FR-11 — Initial Tier assignment | Tier evaluation |
| FR-12 — Cancellation | Membership lifecycle |
| FR-13 — Current Membership | Membership API |
| FR-14 — Expiry | Membership expiry handling |
| NFR-1 — Safe Membership Mutations | Membership mutation rules + transaction/locking rules |
| NFR-2 — Auditability | MembershipAuditLog |
| NFR-3 — Data integrity | DB constraints + validators |
| NFR-4 — Concurrency | Membership optimistic locking |
| NFR-5 — Error handling | Global exception handling |
| NFR-6 — Extensibility | Table-driven Plans/Tiers/PlanBenefits |

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

PaymentRecord
 └── payment-state representation/idempotency only
```

This document is the requirements baseline for the v2 demo implementation and is approved for implementation.

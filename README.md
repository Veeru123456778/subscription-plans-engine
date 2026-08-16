# Subscription Plans Engine

## Run the Project

### Prerequisites

- Java 21
- Maven 3.9+
- Docker Desktop

### 1. Start PostgreSQL

From the project root:

```bash
docker compose up -d
```

Check the container:

```bash
docker ps
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

### 3. Check Health

```bash
curl http://localhost:8080/actuator/health
```

---

# APIs

## API - `GET /api/v1/plans` - Get active plans

Returns all active plans with their active prices.

```bash
curl --location "http://localhost:8080/api/v1/plans"
```

---

## API - `POST /api/v1/admin/plans` - Create a plan

Creates a plan and its initial prices.

```bash
curl --location "http://localhost:8080/api/v1/admin/plans" \
--header "Content-Type: application/json" \
--data '{
  "name": "Premium",
  "rank": 2,
  "consecutiveTierUpgradePrice": 500,
  "prices": [
    {
      "billingPeriod": "MONTHLY",
      "durationDays": 30,
      "price": 999,
      "currency": "INR"
    },
    {
      "billingPeriod": "QUARTERLY",
      "durationDays": 90,
      "price": 2499,
      "currency": "INR"
    },
    {
      "billingPeriod": "YEARLY",
      "durationDays": 365,
      "price": 8999,
      "currency": "INR"
    }
  ]
}'
```

---

## API - `PATCH /api/v1/admin/plans/{planId}` - Update a plan

Updates an existing plan.

```bash
curl --location --request PATCH "http://localhost:8080/api/v1/admin/plans/{planId}" \
--header "Content-Type: application/json" \
--data '{
  "name": "Premium Plus",
  "rank": 3,
  "consecutiveTierUpgradePrice": 750
}'
```

---

## API - `DELETE /api/v1/admin/plans/{planId}` - Disable a plan

Disables an existing plan.

```bash
curl --location --request DELETE "http://localhost:8080/api/v1/admin/plans/{planId}"
```

---

## API - `POST /api/v1/admin/plans/{planId}/prices` - Create a plan price

Creates a new price for an existing plan.

```bash
curl --location "http://localhost:8080/api/v1/admin/plans/{planId}/prices" \
--header "Content-Type: application/json" \
--data '{
  "billingPeriod": "MONTHLY",
  "durationDays": 30,
  "price": 999,
  "currency": "INR"
}'
```

---

## API - `PATCH /api/v1/admin/plans/{planId}/prices/{priceId}` - Update a plan price

Updates an existing price belonging to the specified plan.

```bash
curl --location --request PATCH "http://localhost:8080/api/v1/admin/plans/{planId}/prices/{priceId}" \
--header "Content-Type: application/json" \
--data '{
  "durationDays": 30,
  "price": 1099,
  "currency": "INR"
}'
```

---

## API - `DELETE /api/v1/admin/plans/{planId}/prices/{priceId}` - Disable a plan price

Disables an existing plan price.

```bash
curl --location --request DELETE "http://localhost:8080/api/v1/admin/plans/{planId}/prices/{priceId}"
```

---

## API - `GET /api/v1/tiers` - Get active tiers

Returns all active tiers.

```bash
curl --location "http://localhost:8080/api/v1/tiers"
```

---

## API - `POST /api/v1/admin/tiers` - Create a tier

Creates a tier under a specific plan.

```bash
curl --location "http://localhost:8080/api/v1/admin/tiers" \
--header "Content-Type: application/json" \
--data '{
  "planId": "{planId}",
  "name": "Silver",
  "rank": 1,
  "eligibility": {
    "matchMode": "ALL",
    "rules": [
      {
        "type": "MIN_ORDER_COUNT",
        "value": 5
      }
    ]
  }
}'
```

---

## API - `PATCH /api/v1/admin/tiers/{tierId}` - Update a tier

Updates an existing tier.

```bash
curl --location --request PATCH "http://localhost:8080/api/v1/admin/tiers/{tierId}" \
--header "Content-Type: application/json" \
--data '{
  "name": "Silver",
  "rank": 1,
  "eligibility": {
    "matchMode": "ALL",
    "rules": [
      {
        "type": "MIN_ORDER_COUNT",
        "value": 10
      }
    ]
  },
  "active": true
}'
```

---

## API - `GET /api/v1/admin/plans/{planId}/benefits` - Get active plan benefits

Returns the active benefits configured for a plan.

```bash
curl --location "http://localhost:8080/api/v1/admin/plans/{planId}/benefits"
```

---

## API - `POST /api/v1/admin/plans/{planId}/benefits` - Create a plan benefit

Creates a base Plan benefit or a Tier-specific benefit.

### Base Plan Benefit

```bash
curl --location "http://localhost:8080/api/v1/admin/plans/{planId}/benefits" \
--header "Content-Type: application/json" \
--data '{
  "tierId": null,
  "type": "DISCOUNT",
  "value": 10,
  "discountType": "PERCENT",
  "eligibility": {},
  "monthlyLimit": null
}'
```

### Tier-Specific Benefit

```bash
curl --location "http://localhost:8080/api/v1/admin/plans/{planId}/benefits" \
--header "Content-Type: application/json" \
--data '{
  "tierId": "{tierId}",
  "type": "DISCOUNT",
  "value": 5,
  "discountType": "PERCENT",
  "eligibility": {},
  "monthlyLimit": null
}'
```

---

## API - `PATCH /api/v1/admin/plans/{planId}/benefits/{benefitId}` - Update a plan benefit

Updates an existing PlanBenefit.

```bash
curl --location --request PATCH "http://localhost:8080/api/v1/admin/plans/{planId}/benefits/{benefitId}" \
--header "Content-Type: application/json" \
--data '{
  "value": 15,
  "discountType": "PERCENT",
  "active": true
}'
```

---

## API - `GET /api/v1/membership?userId={userId}` - Get active membership

Returns the active membership for the specified user.

```bash
curl --location "http://localhost:8080/api/v1/membership?userId={userId}"
```

---

## API - `POST /api/v1/membership/subscribe` - Subscribe to a plan

Creates a membership for a user using the selected plan and plan price.

```bash
curl --location "http://localhost:8080/api/v1/membership/subscribe" \
--header "Content-Type: application/json" \
--data '{
  "userId": "{userId}",
  "planId": "{planId}",
  "planPriceId": "{planPriceId}"
}'
```

---

## API - `PATCH /api/v1/membership/{membershipId}/plan` - Change membership plan

Changes the membership to the selected plan and plan price.

```bash
curl --location --request PATCH "http://localhost:8080/api/v1/membership/{membershipId}/plan" \
--header "Content-Type: application/json" \
--data '{
  "planId": "{targetPlanId}",
  "planPriceId": "{targetPlanPriceId}"
}'
```

---

## API - `POST /api/v1/membership/{membershipId}/tier-upgrade` - Upgrade membership tier

Upgrades the membership to the selected tier.

```bash
curl --location --request POST "http://localhost:8080/api/v1/membership/{membershipId}/tier-upgrade" \
--header "Content-Type: application/json" \
--data '{
  "targetTierId": "{targetTierId}"
}'
```

---

## API - `GET /api/v1/membership/{membershipId}/benefits` - Get effective membership benefits

Returns the effective benefits for the membership.

```bash
curl --location "http://localhost:8080/api/v1/membership/{membershipId}/benefits"
```

---

## API - `POST /api/v1/membership/{membershipId}/cancel` - Cancel membership

Cancels the specified membership.

```bash
curl --location --request POST "http://localhost:8080/api/v1/membership/{membershipId}/cancel"
```

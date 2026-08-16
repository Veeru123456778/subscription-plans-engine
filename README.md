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

### 2. Configure Database

Make sure the application is configured to use:

```text
Host: localhost
Port: 5432
Database: membership
User: <POSTGRES_USER>
Password: <POSTGRES_PASSWORD>
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

### 4. Check Health

```bash
curl http://localhost:8080/actuator/health
```

---

# APIs

## API - `GET /api/v1/plans` - Get active plans

Returns all active plans with their active pricing options.

```bash
curl --location "http://localhost:8080/api/v1/plans"
```

---

## API - `POST /api/v1/admin/plans` - Create a plan

Creates a new membership plan with its pricing options.

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

Updates the configurable fields of an existing plan.

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

Creates a new pricing option for an existing plan.

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

Disables an active price belonging to the specified plan.

```bash
curl --location --request DELETE "http://localhost:8080/api/v1/admin/plans/{planId}/prices/{priceId}"
```

---

## API - `GET /api/v1/plans/{planId}/tiers` - Get active tiers for a plan

Returns all active tiers belonging to the specified plan.

```bash
curl --location "http://localhost:8080/api/v1/plans/{planId}/tiers"
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

Updates an existing tier. The tier remains associated with its current plan.

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

Returns active benefits configured for the specified plan.

```bash
curl --location "http://localhost:8080/api/v1/admin/plans/{planId}/benefits"
```

---

## API - `POST /api/v1/admin/plans/{planId}/benefits` - Create a plan benefit

Creates a base Plan benefit or a Tier-specific benefit.

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

Tier-specific benefit:

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

Updates an existing PlanBenefit belonging to the specified plan.

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

## API - `DELETE /api/v1/admin/plans/{planId}/benefits/{benefitId}` - Disable a plan benefit

Disables an active PlanBenefit.

```bash
curl --location --request DELETE "http://localhost:8080/api/v1/admin/plans/{planId}/benefits/{benefitId}"
```

---

## API - `GET /api/v1/memberships/active?userId={userId}` - Get active membership

Returns the active membership for the specified user.

```bash
curl --location "http://localhost:8080/api/v1/memberships/active?userId={userId}"
```

---

## API - `POST /api/v1/memberships` - Subscribe to a plan

Creates a membership for a user using a selected plan and plan price.

```bash
curl --location "http://localhost:8080/api/v1/memberships" \
--header "Content-Type: application/json" \
--data '{
  "userId": "{userId}",
  "planId": "{planId}",
  "planPriceId": "{planPriceId}"
}'
```

---

## API - `PUT /api/v1/memberships/{membershipId}/plan` - Change membership plan

Changes an existing membership to a higher-ranked plan and re-evaluates the tier for the target plan.

```bash
curl --location --request PUT "http://localhost:8080/api/v1/memberships/{membershipId}/plan" \
--header "Content-Type: application/json" \
--data '{
  "planId": "{targetPlanId}",
  "planPriceId": "{targetPlanPriceId}"
}'
```

---

## API - `POST /api/v1/memberships/{membershipId}/tier-upgrade` - Upgrade membership tier

Upgrades the membership to a higher-ranked active tier belonging to the same plan.

```bash
curl --location --request POST "http://localhost:8080/api/v1/memberships/{membershipId}/tier-upgrade" \
--header "Content-Type: application/json" \
--data '{
  "targetTierId": "{targetTierId}"
}'
```

---

## API - `GET /api/v1/memberships/{membershipId}/benefits` - Get effective membership benefits

Returns the effective benefits from the membership's base PlanBenefits and current Tier-specific PlanBenefits.

```bash
curl --location "http://localhost:8080/api/v1/memberships/{membershipId}/benefits"
```

---

## API - `POST /api/v1/memberships/{membershipId}/cancel` - Cancel membership

Cancels the specified active membership.

```bash
curl --location --request POST "http://localhost:8080/api/v1/memberships/{membershipId}/cancel"
```

---

## API - `GET /api/v1/internal/benefits/{userId}` - Get effective benefits for a user

Internal service-to-service API that returns the effective benefits for the user's active membership.

```bash
curl --location "http://localhost:8080/api/v1/internal/benefits/{userId}"
```

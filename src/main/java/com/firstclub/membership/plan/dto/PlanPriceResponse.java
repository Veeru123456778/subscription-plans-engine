package com.firstclub.membership.plan.dto;

import com.firstclub.membership.plan.entity.BillingPeriod;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanPriceResponse(
        UUID id,
        BillingPeriod billingPeriod,
        Integer durationDays,
        BigDecimal price,
        String currency
) {
}
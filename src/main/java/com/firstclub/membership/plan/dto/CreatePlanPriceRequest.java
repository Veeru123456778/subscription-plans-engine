package com.firstclub.membership.plan.dto;

import com.firstclub.membership.plan.entity.BillingPeriod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreatePlanPriceRequest(

        @NotNull
        BillingPeriod billingPeriod,

        @NotNull
        @Min(1)
        Integer durationDays,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        @NotNull
        @Size(min = 3, max = 3)
        String currency
) {
}
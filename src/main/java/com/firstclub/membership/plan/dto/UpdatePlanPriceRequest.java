package com.firstclub.membership.plan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdatePlanPriceRequest(

        @Min(1)
        Integer durationDays,

        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal price,

        @Size(min = 3, max = 3)
        String currency
) {
}
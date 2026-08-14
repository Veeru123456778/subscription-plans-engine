package com.firstclub.membership.plan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.List;

public record UpdatePlanRequest(

        String name,

        @Min(1)
        Integer rank,

        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal consecutiveTierUpgradePrice
) {
}
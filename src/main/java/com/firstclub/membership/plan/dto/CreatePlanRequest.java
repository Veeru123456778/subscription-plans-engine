package com.firstclub.membership.plan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

// These annotations do nothing by themselves — they're metadata. It's the
// @Valid annotation on the controller's method parameter that tells Spring
// to actually read this metadata and reject the request with a 400 before
// our code even runs, if any constraint fails.

public record CreatePlanRequest(

        @NotBlank
        String name,

        @NotNull
        @Min(1)
        Integer rank,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal consecutiveTierUpgradePrice,

        @NotEmpty
        List<@Valid CreatePlanPriceRequest> prices
) {
}
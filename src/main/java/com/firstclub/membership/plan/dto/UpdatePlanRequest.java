// All fields optional here, unlike CreatePlanRequest: a PATCH should let the
// caller update just one field (e.g. only price) without resending everything.
// Each field is only applied by the service if it's non-null — see PlanServiceImpl.


package com.firstclub.membership.plan.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record UpdatePlanRequest(

        @NotBlank
        String name,

        @NotNull
        @Min(1)
        Integer rank,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal consecutiveTierUpgradePrice,

        @Valid
        List<UpdatePlanPriceRequest> prices
) {
}
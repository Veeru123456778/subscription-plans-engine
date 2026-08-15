package com.firstclub.membership.benefit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreatePlanBenefitRequest {

    private UUID tierId;

    @NotBlank
    private String type;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal value;

    private String discountType;

    private Map<String, Object> eligibility;

    @Min(1)
    private Integer monthlyLimit;
}
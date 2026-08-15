package com.firstclub.membership.benefit.dto;

import java.util.Map;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class UpdatePlanBenefitRequest {

    private UUID tierId;

    private String type;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal value;

    private String discountType;

    private Map<String, Object> eligibility;

    @Min(1)
    private Integer monthlyLimit;

    private Boolean active;
}
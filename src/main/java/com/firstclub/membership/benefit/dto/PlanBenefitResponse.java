package com.firstclub.membership.benefit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.Map;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlanBenefitResponse {

    private UUID id;

    private UUID planId;

    private UUID tierId;

    private String type;

    private BigDecimal value;

    private String discountType;

    private Map<String, Object> eligibility;

    private Integer monthlyLimit;

    private boolean active;
}
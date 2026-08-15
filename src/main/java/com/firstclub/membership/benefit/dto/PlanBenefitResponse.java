package com.firstclub.membership.benefit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import com.fasterxml.jackson.databind.JsonNode;
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

    private JsonNode eligibility;

    private Integer monthlyLimit;

    private boolean active;
}
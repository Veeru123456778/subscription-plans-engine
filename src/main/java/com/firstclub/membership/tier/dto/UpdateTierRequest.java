package com.firstclub.membership.tier.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTierRequest {

    private String name;

    @Min(1)
    private Integer rank;

    private JsonNode eligibility;

    private Boolean active;
}
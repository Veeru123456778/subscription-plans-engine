package com.firstclub.membership.tier.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UpdateTierRequest {

    private String name;

    @Min(1)
    private Integer rank;

    private Map<String, Object> eligibility;

    private Boolean active;
}
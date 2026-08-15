package com.firstclub.membership.tier.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTierRequest {

    private String name;

    @Min(1)
    private Integer rank;

    private String eligibility;

    private Boolean active;
}
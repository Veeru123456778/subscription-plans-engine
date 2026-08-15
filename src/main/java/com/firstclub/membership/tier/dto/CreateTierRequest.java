package com.firstclub.membership.tier.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class CreateTierRequest {

    @NotNull
    private UUID planId;

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Integer rank;

    private Map<String, Object> eligibility;
}
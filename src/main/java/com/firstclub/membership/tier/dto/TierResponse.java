package com.firstclub.membership.tier.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class TierResponse {

    private UUID id;

    private String name;

    private Integer rank;

    private String eligibility;

    private boolean active;
}
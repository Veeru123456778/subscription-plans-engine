package com.firstclub.membership.tier.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TierResponse {

    private UUID id;

    private UUID planId;

    private String name;

    private Integer rank;

    private Map<String, Object> eligibility;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;
}
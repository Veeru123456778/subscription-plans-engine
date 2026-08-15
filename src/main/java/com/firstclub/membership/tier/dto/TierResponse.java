package com.firstclub.membership.tier.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class TierResponse {

    private UUID id;

    private String name;

    private Integer rank;

    private JsonNode eligibility;

    private boolean active;
}
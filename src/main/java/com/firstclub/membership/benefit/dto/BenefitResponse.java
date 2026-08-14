package com.firstclub.membership.benefit.dto;

import java.util.UUID;

public class BenefitResponse {

    private final UUID id;
    private final String type;
    private final String value;
    private final String scope;

    public BenefitResponse(
            UUID id,
            String type,
            String value,
            String scope) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.scope = scope;
    }

    public UUID getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getScope() {
        return scope;
    }
}
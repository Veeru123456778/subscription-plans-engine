package com.firstclub.membership.plan.dto;

import java.math.BigDecimal;
import java.util.UUID;

// A DTO, not the entity, is what controllers return. Keeping these separate
// means our public API shape can stay stable even if the Plan entity's
// internal fields change — and it stops JPA-managed objects (with their lazy
// proxies) from leaking into JSON serialization.
public class PlanResponse {

    private final UUID id;
    private final String name;
    private final Integer durationDays;
    private final BigDecimal price;
    private final String currency;

    public PlanResponse(UUID id, String name, Integer durationDays, BigDecimal price, String currency) {
        this.id = id;
        this.name = name;
        this.durationDays = durationDays;
        this.price = price;
        this.currency = currency;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }
}

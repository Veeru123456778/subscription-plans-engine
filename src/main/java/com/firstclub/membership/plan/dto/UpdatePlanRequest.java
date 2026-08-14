package com.firstclub.membership.plan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

// All fields optional here, unlike CreatePlanRequest: a PATCH should let the
// caller update just one field (e.g. only price) without resending everything.
// Each field is only applied by the service if it's non-null — see PlanServiceImpl.

public class UpdatePlanRequest {

    private String name;

    @Min(1)
    private Integer durationDays;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    private String currency;

    private Boolean active;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}

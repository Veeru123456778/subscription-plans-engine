package com.firstclub.membership.plan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// These annotations do nothing by themselves — they're metadata. It's the
// @Valid annotation on the controller's method parameter that tells Spring
// to actually read this metadata and reject the request with a 400 before
// our code even runs, if any constraint fails.

public class CreatePlanRequest {

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Integer durationDays;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

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
}

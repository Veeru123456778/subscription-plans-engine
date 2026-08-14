package com.firstclub.membership.tier.dto;

import com.firstclub.membership.tier.entity.Tier;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateTierRequest {

    @NotBlank
    private String name;

    @NotNull
    @Min(1)
    private Integer rank;

    @Min(0)
    private Integer minOrderCount;

    @DecimalMin(value = "0.0")
    private BigDecimal minOrderValueMonthly;

    private String[] cohortTags;

    @NotNull
    private Tier.MatchMode criteriaMatchMode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getMinOrderCount() {
        return minOrderCount;
    }

    public void setMinOrderCount(Integer minOrderCount) {
        this.minOrderCount = minOrderCount;
    }

    public BigDecimal getMinOrderValueMonthly() {
        return minOrderValueMonthly;
    }

    public void setMinOrderValueMonthly(BigDecimal minOrderValueMonthly) {
        this.minOrderValueMonthly = minOrderValueMonthly;
    }

    public String[] getCohortTags() {
        return cohortTags;
    }

    public void setCohortTags(String[] cohortTags) {
        this.cohortTags = cohortTags;
    }

    public Tier.MatchMode getCriteriaMatchMode() {
        return criteriaMatchMode;
    }

    public void setCriteriaMatchMode(Tier.MatchMode criteriaMatchMode) {
        this.criteriaMatchMode = criteriaMatchMode;
    }
}

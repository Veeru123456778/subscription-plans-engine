package com.firstclub.membership.tier.dto;

import com.firstclub.membership.tier.entity.Tier;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public class UpdateTierRequest {

    private String name;

    @Min(1)
    private Integer rank;

    @Min(0)
    private Integer minOrderCount;

    @DecimalMin(value = "0.0")
    private BigDecimal minOrderValueMonthly;

    private String[] cohortTags;

    private Tier.MatchMode criteriaMatchMode;

    private Boolean active;

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}

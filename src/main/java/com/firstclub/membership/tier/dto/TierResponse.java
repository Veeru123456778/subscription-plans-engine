package com.firstclub.membership.tier.dto;

import com.firstclub.membership.tier.entity.Tier;

import java.math.BigDecimal;
import java.util.UUID;

// No "benefits" field yet — that gets added once the benefit/ package exists
// and TierMapper can pull in each tier's active TierBenefit rows. For now this
// only carries the tier's own fields.

public class TierResponse {

    private final UUID id;
    private final String name;
    private final Integer rank;
    private final Integer minOrderCount;
    private final BigDecimal minOrderValueMonthly;
    private final String[] cohortTags;
    private final Tier.MatchMode criteriaMatchMode;

    public TierResponse(UUID id, String name, Integer rank, Integer minOrderCount,
                         BigDecimal minOrderValueMonthly, String[] cohortTags,
                         Tier.MatchMode criteriaMatchMode) {
        this.id = id;
        this.name = name;
        this.rank = rank;
        this.minOrderCount = minOrderCount;
        this.minOrderValueMonthly = minOrderValueMonthly;
        this.cohortTags = cohortTags;
        this.criteriaMatchMode = criteriaMatchMode;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getRank() {
        return rank;
    }

    public Integer getMinOrderCount() {
        return minOrderCount;
    }

    public BigDecimal getMinOrderValueMonthly() {
        return minOrderValueMonthly;
    }

    public String[] getCohortTags() {
        return cohortTags;
    }

    public Tier.MatchMode getCriteriaMatchMode() {
        return criteriaMatchMode;
    }
}

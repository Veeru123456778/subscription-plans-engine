package com.firstclub.membership.membership.evaluation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TierEvaluationContext {

    private UUID userId;

    private int orderCount;

    private BigDecimal monthlyOrderValue;

    private Set<String> cohortTags;
}
package com.firstclub.membership.tier.mapper;

import com.firstclub.membership.tier.dto.TierResponse;
import com.firstclub.membership.tier.entity.Tier;
import org.springframework.stereotype.Component;

@Component
public class TierMapper {

    public TierResponse toResponse(Tier tier) {
        return new TierResponse(
                tier.getId(),
                tier.getName(),
                tier.getRank(),
                tier.getMinOrderCount(),
                tier.getMinOrderValueMonthly(),
                tier.getCohortTags(),
                tier.getCriteriaMatchMode()
        );
    }
}

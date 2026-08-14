package com.firstclub.membership.plan.mapper;

import com.firstclub.membership.plan.dto.PlanPriceResponse;
import com.firstclub.membership.plan.dto.PlanResponse;
import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.plan.entity.PlanPrice;
import org.springframework.stereotype.Component;

import java.util.List;


// @Component registers this as a Spring-managed bean, so it can be
// @Autowired/constructor-injected into PlanServiceImpl instead of being
// instantiated with "new" by hand everywhere it's needed.
//
// This is hand-written rather than using a mapping library (e.g. MapStruct)
// since it's only 5 fields — a library earns its keep once mapping logic
// gets more repetitive across many entities.

@Component
public class PlanMapper {

    private final PlanPriceMapper planPriceMapper;

    public PlanMapper(PlanPriceMapper planPriceMapper) {
        this.planPriceMapper = planPriceMapper;
    }

    public PlanResponse toResponse(
            Plan plan,
            List<PlanPrice> prices
    ) {
        List<PlanPriceResponse> priceResponses = prices.stream()
                .map(planPriceMapper::toResponse)
                .toList();

        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getRank(),
                plan.getConsecutiveTierUpgradePrice(),
                priceResponses
        );
    }
}
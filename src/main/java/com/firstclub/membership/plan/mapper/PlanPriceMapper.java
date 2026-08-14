package com.firstclub.membership.plan.mapper;

import com.firstclub.membership.plan.dto.PlanPriceResponse;
import com.firstclub.membership.plan.entity.PlanPrice;
import org.springframework.stereotype.Component;

@Component
public class PlanPriceMapper {

    public PlanPriceResponse toResponse(PlanPrice planPrice) {
        return new PlanPriceResponse(
                planPrice.getId(),
                planPrice.getBillingPeriod(),
                planPrice.getDurationDays(),
                planPrice.getPrice(),
                planPrice.getCurrency()
        );
    }
}
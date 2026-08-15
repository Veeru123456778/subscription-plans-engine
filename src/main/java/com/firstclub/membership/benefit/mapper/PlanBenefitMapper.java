package com.firstclub.membership.benefit.mapper;

import com.firstclub.membership.benefit.dto.PlanBenefitResponse;
import com.firstclub.membership.benefit.entity.PlanBenefit;
import org.springframework.stereotype.Component;

@Component
public class PlanBenefitMapper {

    public PlanBenefitResponse toResponse(
            PlanBenefit benefit
    ) {
        return new PlanBenefitResponse(
                benefit.getId(),
                benefit.getPlan().getId(),
                benefit.getTier() != null
                        ? benefit.getTier().getId()
                        : null,
                benefit.getType(),
                benefit.getValue(),
                benefit.getDiscountType(),
                benefit.getEligibility(),
                benefit.getMonthlyLimit(),
                benefit.isActive()
        );
    }
}
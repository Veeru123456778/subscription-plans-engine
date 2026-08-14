package com.firstclub.membership.benefit.mapper;

import com.firstclub.membership.benefit.dto.BenefitResponse;
import com.firstclub.membership.benefit.entity.Benefit;
import org.springframework.stereotype.Component;

// It converts the entity of JPA to response so that it can return the response to controller

@Component
public class BenefitMapper {

    public BenefitResponse toResponse(Benefit benefit) {
        return new BenefitResponse(
                benefit.getId(),
                benefit.getType(),
                benefit.getValue(),
                benefit.getScope()
        );
    }
}
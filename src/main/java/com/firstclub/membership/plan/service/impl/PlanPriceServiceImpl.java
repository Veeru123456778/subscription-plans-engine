package com.firstclub.membership.plan.service.impl;

import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.plan.dto.CreatePlanPriceRequest;
import com.firstclub.membership.plan.dto.PlanPriceResponse;
import com.firstclub.membership.plan.dto.UpdatePlanPriceRequest;
import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.plan.entity.PlanPrice;
import com.firstclub.membership.plan.mapper.PlanPriceMapper;
import com.firstclub.membership.plan.repository.PlanPriceRepository;
import com.firstclub.membership.plan.repository.PlanRepository;
import com.firstclub.membership.plan.service.PlanPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanPriceServiceImpl
        implements PlanPriceService {

    private final PlanRepository planRepository;
    private final PlanPriceRepository planPriceRepository;
    private final PlanPriceMapper planPriceMapper;

    @Override
    public PlanPriceResponse createPrice(
            UUID planId,
            CreatePlanPriceRequest request
    ) {

        Plan plan =
                planRepository.findById(planId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Plan not found: " + planId
                                )
                        );

        PlanPrice planPrice =
                new PlanPrice(
                        plan,
                        request.billingPeriod(),
                        request.durationDays(),
                        request.price(),
                        request.currency()
                );

        PlanPrice savedPrice =
                planPriceRepository.save(planPrice);

        return planPriceMapper.toResponse(
                savedPrice
        );
    }

    @Override
    public PlanPriceResponse updatePrice(
            UUID planId,
            UUID priceId,
            UpdatePlanPriceRequest request
    ) {

        PlanPrice planPrice =
                planPriceRepository
                        .findByIdAndPlanId(
                                priceId,
                                planId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Plan price not found: "
                                                + priceId
                                )
                        );

        if (request.durationDays() != null) {
            planPrice.setDurationDays(
                    request.durationDays()
            );
        }

        if (request.price() != null) {
            planPrice.setPrice(
                    request.price()
            );
        }

        if (request.currency() != null) {
            planPrice.setCurrency(
                    request.currency()
            );
        }

        PlanPrice savedPrice =
                planPriceRepository.save(
                        planPrice
                );

        return planPriceMapper.toResponse(
                savedPrice
        );
    }

    @Override
    public void disablePrice(
            UUID planId,
            UUID priceId
    ) {

        PlanPrice planPrice =
                planPriceRepository
                        .findByIdAndPlanId(
                                priceId,
                                planId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Plan price not found: "
                                                + priceId
                                )
                        );

        planPrice.setActive(false);

        planPriceRepository.save(
                planPrice
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PlanPrice getActivePrice(
            UUID planPriceId,
            UUID planId
    ) {

        PlanPrice planPrice =
                planPriceRepository
                        .findByIdAndPlanId(
                                planPriceId,
                                planId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Plan price not found for selected plan: "
                                                + planPriceId
                                )
                        );

        if (!planPrice.isActive()) {
            throw new ConflictException(
                    "Plan price is inactive: "
                            + planPriceId
            );
        }

        return planPrice;
    }
}
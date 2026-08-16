package com.firstclub.membership.plan.service.impl;

import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.plan.dto.CreatePlanRequest;
import com.firstclub.membership.plan.dto.PlanResponse;
import com.firstclub.membership.plan.dto.UpdatePlanRequest;
import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.plan.entity.PlanPrice;
import com.firstclub.membership.plan.mapper.PlanMapper;
import com.firstclub.membership.plan.repository.PlanPriceRepository;
import com.firstclub.membership.plan.repository.PlanRepository;
import com.firstclub.membership.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanPriceRepository planPriceRepository;
    private final PlanMapper planMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> getActivePlans() {

        return planRepository.findByActiveTrue()
                .stream()
                .map(plan -> {

                    List<PlanPrice> prices =
                            planPriceRepository
                                    .findByPlanAndActiveTrue(plan);

                    return planMapper.toResponse(
                            plan,
                            prices
                    );
                })
                .toList();
    }

    @Override
    public PlanResponse createPlan(
            CreatePlanRequest request
    ) {

        Plan plan = new Plan(
                request.name(),
                request.rank(),
                request.consecutiveTierUpgradePrice()
        );

        Plan savedPlan =
                planRepository.save(plan);

        List<PlanPrice> prices =
                request.prices()
                        .stream()
                        .map(priceRequest ->
                                new PlanPrice(
                                        savedPlan,
                                        priceRequest.billingPeriod(),
                                        priceRequest.durationDays(),
                                        priceRequest.price(),
                                        priceRequest.currency()
                                )
                        )
                        .toList();

        List<PlanPrice> savedPrices =
                planPriceRepository.saveAll(prices);

        return planMapper.toResponse(
                savedPlan,
                savedPrices
        );
    }

    @Override
    public PlanResponse updatePlan(
            UUID planId,
            UpdatePlanRequest request
    ) {

        Plan plan = getPlan(planId);

        if (request.name() != null) {
            plan.setName(request.name());
        }

        if (request.rank() != null) {
            plan.setRank(request.rank());
        }

        if (request.consecutiveTierUpgradePrice() != null) {
            plan.setConsecutiveTierUpgradePrice(
                    request.consecutiveTierUpgradePrice()
            );
        }

        Plan savedPlan =
                planRepository.save(plan);

        List<PlanPrice> prices =
                planPriceRepository
                        .findByPlanAndActiveTrue(savedPlan);

        return planMapper.toResponse(
                savedPlan,
                prices
        );
    }

    @Override
    public void disablePlan(UUID planId) {

        Plan plan = getPlan(planId);

        plan.setActive(false);

        planRepository.save(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public Plan getPlan(UUID planId) {

        return planRepository.findById(planId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Plan not found: " + planId
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Plan getActivePlan(UUID planId) {

        Plan plan = getPlan(planId);

        if (!plan.isActive()) {
            throw new ConflictException(
                    "Plan is inactive: " + planId
            );
        }

        return plan;
    }
}
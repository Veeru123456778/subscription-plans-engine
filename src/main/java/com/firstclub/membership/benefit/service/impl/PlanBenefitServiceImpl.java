package com.firstclub.membership.benefit.service.impl;

import com.firstclub.membership.benefit.dto.CreatePlanBenefitRequest;
import com.firstclub.membership.benefit.dto.PlanBenefitResponse;
import com.firstclub.membership.benefit.dto.UpdatePlanBenefitRequest;
import com.firstclub.membership.benefit.entity.PlanBenefit;
import com.firstclub.membership.benefit.mapper.PlanBenefitMapper;
import com.firstclub.membership.benefit.repository.PlanBenefitRepository;
import com.firstclub.membership.benefit.service.PlanBenefitService;
import com.firstclub.membership.benefit.validation.BenefitTypeValidator;
import com.firstclub.membership.common.exception.BadRequestException;
import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.plan.repository.PlanRepository;
import com.firstclub.membership.tier.entity.Tier;
import com.firstclub.membership.tier.repository.TierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanBenefitServiceImpl
        implements PlanBenefitService {

    private final PlanBenefitRepository planBenefitRepository;
    private final PlanRepository planRepository;
    private final TierRepository tierRepository;
    private final PlanBenefitMapper planBenefitMapper;
    private final BenefitTypeValidator benefitTypeValidator;

    @Override
    @Transactional(readOnly = true)
    public List<PlanBenefitResponse> getActiveBenefits(
            UUID planId
    ) {

        Plan plan =
                getPlan(planId);

        return planBenefitRepository
                .findByPlanAndActiveTrue(plan)
                .stream()
                .map(planBenefitMapper::toResponse)
                .toList();
    }

    @Override
    public PlanBenefitResponse createBenefit(
            UUID planId,
            CreatePlanBenefitRequest request
    ) {

        Plan plan =
                getPlan(planId);

        benefitTypeValidator.validate(
                request.getType()
        );

        Tier tier =
                getTier(request.getTierId());

        validateDiscountConfiguration(
                request.getType(),
                request.getValue(),
                request.getDiscountType()
        );

        validateDuplicate(
                plan,
                tier,
                request.getType(),
                null
        );

        PlanBenefit benefit =
                new PlanBenefit(
                        plan,
                        tier,
                        request.getType(),
                        request.getValue(),
                        request.getDiscountType(),
                        request.getEligibility(),
                        request.getMonthlyLimit()
                );

        return planBenefitMapper.toResponse(
                planBenefitRepository.save(
                        benefit
                )
        );
    }

    @Override
    public PlanBenefitResponse updateBenefit(
            UUID planId,
            UUID benefitId,
            UpdatePlanBenefitRequest request
    ) {

        Plan plan =
                getPlan(planId);

        PlanBenefit benefit =
                planBenefitRepository.findById(
                        benefitId
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Plan benefit not found: "
                                        + benefitId
                        )
                );

        if (!benefit.getPlan().getId()
                .equals(planId)) {

            throw new ResourceNotFoundException(
                    "Plan benefit not found: "
                            + benefitId
            );
        }

        if (request.getType() != null) {
            benefitTypeValidator.validate(
                    request.getType()
            );
        }

        String type =
                request.getType() != null
                        ? request.getType()
                        : benefit.getType();

        UUID tierId =
                request.getTierId() != null
                        ? request.getTierId()
                        : benefit.getTier() != null
                        ? benefit.getTier().getId()
                        : null;

        Tier tier =
                getTier(tierId);

        validateDiscountConfiguration(
                type,
                request.getValue() != null
                        ? request.getValue()
                        : benefit.getValue(),
                request.getDiscountType() != null
                        ? request.getDiscountType()
                        : benefit.getDiscountType()
        );

        validateDuplicate(
                plan,
                tier,
                type,
                benefitId
        );

        if (request.getTierId() != null) {
            benefit.setTier(tier);
        }

        if (request.getType() != null) {
            benefit.setType(
                    request.getType()
            );
        }

        if (request.getValue() != null) {
            benefit.setValue(
                    request.getValue()
            );
        }

        if (request.getDiscountType() != null) {
            benefit.setDiscountType(
                    request.getDiscountType()
            );
        }

        if (request.getEligibility() != null) {
            benefit.setEligibility(
                    request.getEligibility()
            );
        }

        if (request.getMonthlyLimit() != null) {
            benefit.setMonthlyLimit(
                    request.getMonthlyLimit()
            );
        }

        if (request.getActive() != null) {
            benefit.setActive(
                    request.getActive()
            );
        }

        return planBenefitMapper.toResponse(
                benefit
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanBenefit> getEffectiveBenefits(
            Plan plan,
            Tier tier
    ) {

        List<PlanBenefit> benefits =
                new ArrayList<>(
                        planBenefitRepository
                                .findByPlanAndActiveTrue(
                                        plan
                                )
                );

        if (tier != null) {
            benefits.addAll(
                    planBenefitRepository
                            .findByPlanAndTierAndActiveTrue(
                                    plan,
                                    tier
                            )
            );
        }

        return benefits;
    }

    private Plan getPlan(
            UUID planId
    ) {

        return planRepository.findById(
                planId
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Plan not found: " + planId
                )
        );
    }

    private Tier getTier(
            UUID tierId
    ) {

        if (tierId == null) {
            return null;
        }

        return tierRepository.findById(
                tierId
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Tier not found: " + tierId
                )
        );
    }

    private void validateDuplicate(
            Plan plan,
            Tier tier,
            String type,
            UUID benefitId
    ) {

        boolean exists;

        if (tier == null) {

            exists =
                    benefitId == null
                            ? planBenefitRepository
                            .existsByPlanAndTypeAndTierIsNull(
                                    plan,
                                    type
                            )
                            : planBenefitRepository
                            .existsByPlanAndTypeAndTierIsNullAndIdNot(
                                    plan,
                                    type,
                                    benefitId
                            );

        } else {

            exists =
                    benefitId == null
                            ? planBenefitRepository
                            .existsByPlanAndTierAndType(
                                    plan,
                                    tier,
                                    type
                            )
                            : planBenefitRepository
                            .existsByPlanAndTierAndTypeAndIdNot(
                                    plan,
                                    tier,
                                    type,
                                    benefitId
                            );
        }

        if (exists) {
            throw new ConflictException(
                    "Benefit of type "
                            + type
                            + " already exists for this Plan/Tier"
            );
        }
    }

    private void validateDiscountConfiguration(
            String type,
            BigDecimal value,
            String discountType
    ) {

        if ("DISCOUNT".equals(type)) {

            if (value == null) {
                throw new BadRequestException(
                        "Discount value is required"
                );
            }

            if (discountType == null) {
                throw new BadRequestException(
                        "Discount type is required for DISCOUNT"
                );
            }

            if (!"PERCENT".equals(discountType)
                    && !"FLAT".equals(discountType)) {

                throw new BadRequestException(
                        "Unsupported discount type: "
                                + discountType
                );
            }

            if ("PERCENT".equals(discountType)
                    && value.compareTo(
                    BigDecimal.valueOf(100)
            ) > 0) {

                throw new BadRequestException(
                        "Percentage discount cannot exceed 100"
                );
            }

        } else if (discountType != null) {

            throw new BadRequestException(
                    "Discount type is only valid for DISCOUNT benefits"
            );
        }
    }
}
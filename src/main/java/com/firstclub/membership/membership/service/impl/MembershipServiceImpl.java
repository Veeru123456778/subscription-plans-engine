package com.firstclub.membership.membership.service.impl;

import com.firstclub.membership.benefit.dto.PlanBenefitResponse;
import com.firstclub.membership.benefit.entity.PlanBenefit;
import com.firstclub.membership.benefit.mapper.PlanBenefitMapper;
import com.firstclub.membership.benefit.service.PlanBenefitService;
import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.membership.dto.ChangeMembershipPlanRequest;
import com.firstclub.membership.membership.dto.MembershipBenefitsResponse;
import com.firstclub.membership.membership.dto.MembershipResponse;
import com.firstclub.membership.membership.dto.SubscribeRequest;
import com.firstclub.membership.membership.dto.TierUpgradeRequest;
import com.firstclub.membership.membership.entity.Membership;
import com.firstclub.membership.membership.entity.MembershipStatus;
import com.firstclub.membership.membership.entity.TierSource;
import com.firstclub.membership.membership.evaluation.TierAssignmentService;
import com.firstclub.membership.membership.mapper.MembershipMapper;
import com.firstclub.membership.membership.repository.MembershipRepository;
import com.firstclub.membership.membership.service.MembershipService;
import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.plan.entity.PlanPrice;
import com.firstclub.membership.plan.service.PlanPriceService;
import com.firstclub.membership.plan.service.PlanService;
import com.firstclub.membership.tier.entity.Tier;
import com.firstclub.membership.tier.service.TierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipServiceImpl
        implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipMapper membershipMapper;

    private final PlanService planService;
    private final PlanPriceService planPriceService;
    private final TierService tierService;
    private final TierAssignmentService tierAssignmentService;
    private final PlanBenefitService planBenefitService;
    private final PlanBenefitMapper planBenefitMapper;

    @Override
    @Transactional
    public MembershipResponse getActiveMembership(
            UUID userId
    ) {

        Membership membership =
                membershipRepository
                        .findByUserIdAndStatus(
                                userId,
                                MembershipStatus.ACTIVE
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Active membership not found for user: "
                                                + userId
                                )
                        );

        if (!membership.getExpiryDate()
                .isAfter(Instant.now())) {

            membership.setStatus(
                    MembershipStatus.EXPIRED
            );

            membershipRepository.save(
                    membership
            );

            throw new ResourceNotFoundException(
                    "Active membership not found for user: "
                            + userId
            );
        }

        return membershipMapper.toResponse(
                membership
        );
    }

    @Override
    public MembershipResponse subscribe(
            SubscribeRequest request
    ) {

        if (membershipRepository.existsByUserIdAndStatus(
                request.getUserId(),
                MembershipStatus.ACTIVE
        )) {

            throw new ConflictException(
                    "User already has an active membership"
            );
        }

        Plan plan =
                planService.getActivePlan(
                        request.getPlanId()
                );

        PlanPrice planPrice =
                planPriceService.getActivePrice(
                        request.getPlanPriceId(),
                        plan.getId()
                );

        var initialTier =
                tierAssignmentService
                        .determineInitialTier(
                                request.getUserId(),
                                plan.getId()
                        );

        Instant startDate =
                Instant.now();

        Instant expiryDate =
                startDate.plus(
                        Duration.ofDays(
                                planPrice.getDurationDays()
                        )
                );

        Membership membership =
                new Membership(
                        request.getUserId(),
                        plan,
                        planPrice,
                        initialTier.orElse(null),
                        TierSource.AUTO,
                        startDate,
                        expiryDate
                );

        Membership saved =
                membershipRepository.save(
                        membership
                );

        return membershipMapper.toResponse(
                saved
        );
    }

    @Override
    public MembershipResponse changePlan(
            UUID membershipId,
            ChangeMembershipPlanRequest request
    ) {

        Membership membership =
                getActiveMembershipEntity(
                        membershipId
                );

        Plan targetPlan =
                planService.getActivePlan(
                        request.getPlanId()
                );

        PlanPrice targetPrice =
                planPriceService.getActivePrice(
                        request.getPlanPriceId(),
                        targetPlan.getId()
                );

        if (membership.getPlan().getId()
                .equals(targetPlan.getId())
                && membership.getPlanPrice().getId()
                .equals(targetPrice.getId())) {

            throw new ConflictException(
                    "Membership is already on the selected plan and price"
            );
        }

        Instant startDate =
                Instant.now();

        Instant expiryDate =
                startDate.plus(
                        Duration.ofDays(
                                targetPrice.getDurationDays()
                        )
                );

        /*
         * The old Tier belongs to the old Plan.
         *
         * Re-evaluate against the target Plan only.
         */
        var newTier =
                tierAssignmentService
                        .determineInitialTier(
                                membership.getUserId(),
                                targetPlan.getId()
                        );

        membership.setPlan(
                targetPlan
        );

        membership.setPlanPrice(
                targetPrice
        );

        membership.setCurrentTier(
                newTier.orElse(null)
        );

        membership.setTierSource(
                TierSource.AUTO
        );

        membership.setStartDate(
                startDate
        );

        membership.setExpiryDate(
                expiryDate
        );

        Membership saved =
                membershipRepository.save(
                        membership
                );

        return membershipMapper.toResponse(
                saved
        );
    }

    @Override
    public MembershipResponse upgradeTier(
            UUID membershipId,
            TierUpgradeRequest request
    ) {

        Membership membership =
                getActiveMembershipEntity(
                        membershipId
                );

        if (membership.getCurrentTier() == null) {

            throw new ConflictException(
                    "Membership does not have a current tier"
            );
        }

        Tier targetTier =
                tierService.getActiveTier(
                        request.getTargetTierId()
                );

        /*
         * Tier upgrades are only allowed inside the
         * Membership's current Plan.
         */
        if (!targetTier.getPlan().getId()
                .equals(membership.getPlan().getId())) {

            throw new ConflictException(
                    "Target tier does not belong to membership plan"
            );
        }

        Tier currentTier =
                membership.getCurrentTier();

        if (targetTier.getRank()
                <= currentTier.getRank()) {

            throw new ConflictException(
                    "Target tier must be higher than current tier"
            );
        }

        int rankDifference =
                targetTier.getRank()
                        - currentTier.getRank();

        BigDecimal upgradePrice =
                membership.getPlan()
                        .getConsecutiveTierUpgradePrice()
                        .multiply(
                                BigDecimal.valueOf(
                                        rankDifference
                                )
                        );

        /*
         * V1 DEMO:
         * Payment is assumed to be successful.
         */
        if (upgradePrice.signum() < 0) {

            throw new ConflictException(
                    "Invalid tier upgrade price"
            );
        }

        membership.setCurrentTier(
                targetTier
        );

        membership.setTierSource(
                TierSource.PAID_UPGRADE
        );

        Membership saved =
                membershipRepository.save(
                        membership
                );

        return membershipMapper.toResponse(
                saved
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipBenefitsResponse getEffectiveBenefits(
            UUID membershipId
    ) {

        Membership membership =
                getActiveMembershipEntity(
                        membershipId
                );

        List<PlanBenefit> benefits =
                planBenefitService.getEffectiveBenefits(
                        membership.getPlan(),
                        membership.getCurrentTier()
                );

        List<PlanBenefitResponse> responses =
                benefits.stream()
                        .map(planBenefitMapper::toResponse)
                        .toList();

        return new MembershipBenefitsResponse(
                membership.getId(),
                membership.getPlan().getId(),
                membership.getCurrentTier() != null
                        ? membership.getCurrentTier().getId()
                        : null,
                responses
        );
    }

    @Override
    public MembershipResponse cancel(
            UUID membershipId
    ) {

        Membership membership =
                getActiveMembershipEntity(
                        membershipId
                );

        membership.setStatus(
                MembershipStatus.CANCELLED
        );

        Membership saved =
                membershipRepository.save(
                        membership
                );

        return membershipMapper.toResponse(
                saved
        );
    }

    private Membership getActiveMembershipEntity(
            UUID membershipId
    ) {

        Membership membership =
                membershipRepository.findById(
                        membershipId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Membership not found: "
                                        + membershipId
                        )
                );

        if (membership.getStatus()
                != MembershipStatus.ACTIVE) {

            throw new ConflictException(
                    "Membership is not active: "
                            + membershipId
            );
        }

        if (!membership.getExpiryDate()
                .isAfter(Instant.now())) {

            membership.setStatus(
                    MembershipStatus.EXPIRED
            );

            membershipRepository.save(
                    membership
            );

            throw new ConflictException(
                    "Membership has expired: "
                            + membershipId
            );
        }

        return membership;
    }
}
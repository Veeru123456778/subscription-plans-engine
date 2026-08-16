package com.firstclub.membership.tier.service.impl;

import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.plan.repository.PlanRepository;
import com.firstclub.membership.tier.dto.CreateTierRequest;
import com.firstclub.membership.tier.dto.TierResponse;
import com.firstclub.membership.tier.dto.UpdateTierRequest;
import com.firstclub.membership.tier.entity.Tier;
import com.firstclub.membership.tier.mapper.TierMapper;
import com.firstclub.membership.tier.repository.TierRepository;
import com.firstclub.membership.tier.service.TierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TierServiceImpl implements TierService {

    private final TierRepository tierRepository;
    private final TierMapper tierMapper;
    private final PlanRepository planRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TierResponse> getActiveTiers() {

        return tierRepository
                .findByActiveTrueOrderByRankDesc()
                .stream()
                .map(tierMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TierResponse> getActiveTiersByPlan(
            UUID planId
    ) {

        if (!planRepository.existsById(planId)) {
            throw new ResourceNotFoundException(
                    "Plan not found: " + planId
            );
        }

        return tierRepository
                .findByPlanIdAndActiveTrueOrderByRankDesc(
                        planId
                )
                .stream()
                .map(tierMapper::toResponse)
                .toList();
    }

    @Override
    public TierResponse createTier(
            CreateTierRequest request
    ) {

        Plan plan =
                planRepository.findById(
                        request.getPlanId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Plan not found: "
                                        + request.getPlanId()
                        )
                );

        if (tierRepository
                .existsByPlanIdAndNameIgnoreCaseAndActiveTrue(
                        request.getPlanId(),
                        request.getName()
                )) {

            throw new ConflictException(
                    "An active tier named '"
                            + request.getName()
                            + "' already exists for this plan"
            );
        }

        if (tierRepository
                .existsByPlanIdAndRankAndActiveTrue(
                        request.getPlanId(),
                        request.getRank()
                )) {

            throw new ConflictException(
                    "An active tier with rank "
                            + request.getRank()
                            + " already exists for this plan"
            );
        }

        Tier tier =
                new Tier(
                        plan,
                        request.getName(),
                        request.getRank(),
                        request.getEligibility()
                );

        Tier savedTier =
                tierRepository.save(tier);

        return tierMapper.toResponse(
                savedTier
        );
    }

    @Override
    public TierResponse updateTier(
            UUID tierId,
            UpdateTierRequest request
    ) {

        Tier tier =
                getTier(tierId);

        UUID planId =
                tier.getPlan().getId();

        boolean resultingActive =
                request.getActive() != null
                        ? request.getActive()
                        : tier.isActive();

        if (resultingActive
                && request.getName() != null
                && tierRepository
                        .existsByPlanIdAndNameIgnoreCaseAndActiveTrueAndIdNot(
                                planId,
                                request.getName(),
                                tierId
                        )) {

            throw new ConflictException(
                    "An active tier named '"
                            + request.getName()
                            + "' already exists for this plan"
            );
        }

        if (resultingActive
                && request.getRank() != null
                && tierRepository
                        .existsByPlanIdAndRankAndActiveTrueAndIdNot(
                                planId,
                                request.getRank(),
                                tierId
                        )) {

            throw new ConflictException(
                    "An active tier with rank "
                            + request.getRank()
                            + " already exists for this plan"
            );
        }

        if (request.getName() != null) {
            tier.setName(
                    request.getName()
            );
        }

        if (request.getRank() != null) {
            tier.setRank(
                    request.getRank()
            );
        }

        if (request.getEligibility() != null) {
            tier.setEligibility(
                    request.getEligibility()
            );
        }

        if (request.getActive() != null) {
            tier.setActive(
                    request.getActive()
            );
        }

        return tierMapper.toResponse(
                tier
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Tier getTier(
            UUID tierId
    ) {

        return tierRepository.findById(
                tierId
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Tier not found: " + tierId
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Tier getActiveTier(
            UUID tierId
    ) {

        Tier tier =
                getTier(tierId);

        if (!tier.isActive()) {
            throw new ConflictException(
                    "Target tier is inactive: "
                            + tierId
            );
        }

        return tier;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tier> getActiveTierEntitiesByPlan(
            UUID planId
    ) {

        if (!planRepository.existsById(planId)) {
            throw new ResourceNotFoundException(
                    "Plan not found: " + planId
            );
        }

        return tierRepository
                .findByPlanIdAndActiveTrueOrderByRankDesc(
                        planId
                );
    }
}
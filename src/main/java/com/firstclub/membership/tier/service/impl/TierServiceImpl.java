package com.firstclub.membership.tier.service.impl;

import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.tier.dto.CreateTierRequest;
import com.firstclub.membership.tier.dto.TierResponse;
import com.firstclub.membership.tier.dto.UpdateTierRequest;
import com.firstclub.membership.tier.entity.Tier;
import com.firstclub.membership.tier.mapper.TierMapper;
import com.firstclub.membership.tier.repository.TierRepository;
import com.firstclub.membership.tier.service.TierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TierServiceImpl implements TierService {

    private final TierRepository tierRepository;
    private final TierMapper tierMapper;

    public TierServiceImpl(TierRepository tierRepository, TierMapper tierMapper) {
        this.tierRepository = tierRepository;
        this.tierMapper = tierMapper;
    }

    @Override
    public List<TierResponse> getActiveTiers() {
        return tierRepository.findByActiveTrueOrderByRankDesc().stream()
                .map(tierMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TierResponse createTier(CreateTierRequest request) {
        if (tierRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("A tier named '" + request.getName() + "' already exists");
        }
        if (tierRepository.existsByRank(request.getRank())) {
            throw new ConflictException("Rank " + request.getRank() + " is already assigned to another tier");
        }

        Tier tier = new Tier(request.getName(), request.getRank(), request.getCriteriaMatchMode());
        tier.setMinOrderCount(request.getMinOrderCount());
        tier.setMinOrderValueMonthly(request.getMinOrderValueMonthly());
        tier.setCohortTags(request.getCohortTags());

        Tier saved = tierRepository.save(tier);
        return tierMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TierResponse updateTier(UUID tierId, UpdateTierRequest request) {

        Tier tier = tierRepository.findById(tierId)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found: " + tierId));

        if (request.getName() != null && tierRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), tierId)) {
            throw new ConflictException(
                    "A tier named '" + request.getName() + "' already exists");
        }

        if (request.getRank() != null
                && tierRepository.existsByRankAndIdNot(
                        request.getRank(), tierId)) {
            throw new ConflictException(
                    "Rank " + request.getRank()
                            + " is already assigned to another tier");
        }
      
        if (request.getName() != null) {
            tier.setName(request.getName());
        }
        if (request.getRank() != null) {
            tier.setRank(request.getRank());
        }
        if (request.getMinOrderCount() != null) {
            tier.setMinOrderCount(request.getMinOrderCount());
        }
        if (request.getMinOrderValueMonthly() != null) {
            tier.setMinOrderValueMonthly(request.getMinOrderValueMonthly());
        }
        if (request.getCohortTags() != null) {
            tier.setCohortTags(request.getCohortTags());
        }
        if (request.getCriteriaMatchMode() != null) {
            tier.setCriteriaMatchMode(request.getCriteriaMatchMode());
        }
        if (request.getActive() != null) {
            tier.setActive(request.getActive());
        }

        return tierMapper.toResponse(tier);
    }
}

package com.firstclub.membership.tier.service;

import com.firstclub.membership.tier.dto.CreateTierRequest;
import com.firstclub.membership.tier.dto.TierResponse;
import com.firstclub.membership.tier.dto.UpdateTierRequest;
import com.firstclub.membership.tier.entity.Tier;

import java.util.List;
import java.util.UUID;

public interface TierService {

    List<TierResponse> getActiveTiers();

    List<TierResponse> getActiveTiersByPlan(
            UUID planId
    );

    TierResponse createTier(
            CreateTierRequest request
    );

    TierResponse updateTier(
            UUID tierId,
            UpdateTierRequest request
    );

    Tier getTier(UUID tierId);

    Tier getActiveTier(UUID tierId);

    List<Tier> getActiveTierEntitiesByPlan(
            UUID planId
    );
}
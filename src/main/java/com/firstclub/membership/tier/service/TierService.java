package com.firstclub.membership.tier.service;

import com.firstclub.membership.tier.dto.CreateTierRequest;
import com.firstclub.membership.tier.dto.TierResponse;
import com.firstclub.membership.tier.dto.UpdateTierRequest;

import java.util.List;
import java.util.UUID;

public interface TierService {

    List<TierResponse> getActiveTiers();

    TierResponse createTier(CreateTierRequest request);

    TierResponse updateTier(UUID tierId, UpdateTierRequest request);
}

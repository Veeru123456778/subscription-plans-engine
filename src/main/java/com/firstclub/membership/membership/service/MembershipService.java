package com.firstclub.membership.membership.service;

import com.firstclub.membership.membership.dto.ChangePlanRequest;
import com.firstclub.membership.membership.dto.MembershipBenefitsResponse;
import com.firstclub.membership.membership.dto.MembershipResponse;
import com.firstclub.membership.membership.dto.SubscribeRequest;
import com.firstclub.membership.membership.dto.TierUpgradeRequest;

import java.util.UUID;

public interface MembershipService {

    MembershipResponse getActiveMembership(
            UUID userId
    );

    MembershipResponse subscribe(
            SubscribeRequest request
    );

    MembershipResponse changePlan(
            UUID membershipId,
            ChangePlanRequest request
    );

    MembershipResponse upgradeTier(
            UUID membershipId,
            TierUpgradeRequest request
    );

    MembershipBenefitsResponse getEffectiveBenefits(
            UUID membershipId
    );

    MembershipResponse cancel(
            UUID membershipId
    );
}
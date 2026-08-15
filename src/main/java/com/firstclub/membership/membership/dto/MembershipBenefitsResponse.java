package com.firstclub.membership.membership.dto;

import com.firstclub.membership.benefit.dto.PlanBenefitResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MembershipBenefitsResponse {

    private UUID membershipId;

    private UUID planId;

    private UUID tierId;

    private List<PlanBenefitResponse> benefits;
}
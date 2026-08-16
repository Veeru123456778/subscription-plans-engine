package com.firstclub.membership.benefit.service;

import com.firstclub.membership.benefit.dto.CreatePlanBenefitRequest;
import com.firstclub.membership.benefit.dto.PlanBenefitResponse;
import com.firstclub.membership.benefit.dto.UpdatePlanBenefitRequest;
import com.firstclub.membership.benefit.entity.PlanBenefit;
import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.tier.entity.Tier;

import java.util.List;
import java.util.UUID;

public interface PlanBenefitService {

    List<PlanBenefitResponse> getActiveBenefits(
            UUID planId
    );

    PlanBenefitResponse createBenefit(
            UUID planId,
            CreatePlanBenefitRequest request
    );

    PlanBenefitResponse updateBenefit(
            UUID planId,
            UUID benefitId,
            UpdatePlanBenefitRequest request
    );

    List<PlanBenefit> getEffectiveBenefits(
            Plan plan,
            Tier tier
    );
}
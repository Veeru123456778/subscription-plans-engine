package com.firstclub.membership.plan.service;

import com.firstclub.membership.plan.dto.CreatePlanRequest;
import com.firstclub.membership.plan.dto.PlanResponse;
import com.firstclub.membership.plan.dto.UpdatePlanRequest;
import com.firstclub.membership.plan.entity.Plan;

import java.util.List;
import java.util.UUID;

public interface PlanService {

    List<PlanResponse> getActivePlans();

    PlanResponse createPlan(CreatePlanRequest request);

    PlanResponse updatePlan(
            UUID planId,
            UpdatePlanRequest request
    );

    void disablePlan(UUID planId);

    Plan getPlan(UUID planId);

    Plan getActivePlan(UUID planId);
}
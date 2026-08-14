package com.firstclub.membership.plan.service;

import com.firstclub.membership.plan.dto.CreatePlanRequest;
import com.firstclub.membership.plan.dto.PlanResponse;
import com.firstclub.membership.plan.dto.UpdatePlanRequest;

import java.util.List;
import java.util.UUID;

// Controllers depend on this interface, not PlanServiceImpl directly. In a single-implementation service like this it mainly pays off for testing — a test can mock PlanService without knowing anything about how it'sactually implemented.

public interface PlanService {

    List<PlanResponse> getActivePlans();

    PlanResponse createPlan(CreatePlanRequest request);

    PlanResponse updatePlan(UUID planId, UpdatePlanRequest request);

    void disablePlan(UUID planId);
}

package com.firstclub.membership.plan.controller;

import com.firstclub.membership.plan.dto.CreatePlanRequest;
import com.firstclub.membership.plan.dto.PlanResponse;
import com.firstclub.membership.plan.dto.UpdatePlanRequest;
import com.firstclub.membership.plan.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


// on it, so admin auth rules (added later in SecurityConfig) can be scoped to
// this whole class by URL pattern ("/api/v1/admin/**") in one place.
@RestController
@RequestMapping("/api/v1/admin/plans")
public class PlanAdminController {

    private final PlanService planService;

    public PlanAdminController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    public ResponseEntity<PlanResponse> createPlan(
            @Valid @RequestBody CreatePlanRequest request
    ) {
        PlanResponse created = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{planId}")
    public PlanResponse updatePlan(
            @PathVariable UUID planId,
            @Valid @RequestBody UpdatePlanRequest request
    ) {
        return planService.updatePlan(planId, request);
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> disablePlan(
            @PathVariable UUID planId
    ) {
        planService.disablePlan(planId);
        return ResponseEntity.noContent().build();
    }
}
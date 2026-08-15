package com.firstclub.membership.benefit.controller;

import com.firstclub.membership.benefit.dto.CreatePlanBenefitRequest;
import com.firstclub.membership.benefit.dto.PlanBenefitResponse;
import com.firstclub.membership.benefit.dto.UpdatePlanBenefitRequest;
import com.firstclub.membership.benefit.service.PlanBenefitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/plans/{planId}/benefits")
@RequiredArgsConstructor
public class PlanBenefitAdminController {

    private final PlanBenefitService planBenefitService;

    @GetMapping
    public List<PlanBenefitResponse> getActiveBenefits(
            @PathVariable UUID planId
    ) {

        return planBenefitService.getActiveBenefits(planId);
    }

    @PostMapping
    public ResponseEntity<PlanBenefitResponse> createBenefit(
            @PathVariable UUID planId,
            @Valid @RequestBody CreatePlanBenefitRequest request
    ) {

        PlanBenefitResponse created =
                planBenefitService.createBenefit(
                        planId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @PatchMapping("/{benefitId}")
    public PlanBenefitResponse updateBenefit(
            @PathVariable UUID planId,
            @PathVariable UUID benefitId,
            @Valid @RequestBody UpdatePlanBenefitRequest request
    ) {

        return planBenefitService.updateBenefit(
                planId,
                benefitId,
                request
        );
    }
}
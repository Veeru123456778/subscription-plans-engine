package com.firstclub.membership.plan.controller;

import com.firstclub.membership.plan.dto.CreatePlanPriceRequest;
import com.firstclub.membership.plan.dto.PlanPriceResponse;
import com.firstclub.membership.plan.dto.UpdatePlanPriceRequest;
import com.firstclub.membership.plan.service.PlanPriceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/plans/{planId}/prices")
public class PlanPriceAdminController {

    private final PlanPriceService planPriceService;

    public PlanPriceAdminController(
            PlanPriceService planPriceService
    ) {
        this.planPriceService = planPriceService;
    }

    @PostMapping
    public ResponseEntity<PlanPriceResponse> createPrice(
            @PathVariable UUID planId,
            @Valid @RequestBody CreatePlanPriceRequest request
    ) {
        PlanPriceResponse created =
                planPriceService.createPrice(planId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @PatchMapping("/{priceId}")
    public PlanPriceResponse updatePrice(
            @PathVariable UUID planId,
            @PathVariable UUID priceId,
            @Valid @RequestBody UpdatePlanPriceRequest request
    ) {
        return planPriceService.updatePrice(
                planId,
                priceId,
                request
        );
    }

    @DeleteMapping("/{priceId}")
    public ResponseEntity<Void> disablePrice(
            @PathVariable UUID planId,
            @PathVariable UUID priceId
    ) {
        planPriceService.disablePrice(planId, priceId);
        return ResponseEntity.noContent().build();
    }
}
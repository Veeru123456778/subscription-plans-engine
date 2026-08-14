package com.firstclub.membership.benefit.controller;

import com.firstclub.membership.benefit.dto.BenefitResponse;
import com.firstclub.membership.benefit.dto.CreateBenefitRequest;
import com.firstclub.membership.benefit.dto.UpdateBenefitRequest;
import com.firstclub.membership.benefit.service.BenefitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/benefits")
public class BenefitAdminController {

    private final BenefitService benefitService;

    public BenefitAdminController(BenefitService benefitService) {
        this.benefitService = benefitService;
    }

    @PostMapping
    public ResponseEntity<BenefitResponse> createBenefit(
            @Valid @RequestBody CreateBenefitRequest request) {

        BenefitResponse created = benefitService.createBenefit(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @PatchMapping("/{benefitId}")
    public BenefitResponse updateBenefit(
            @PathVariable UUID benefitId,
            @Valid @RequestBody UpdateBenefitRequest request) {

        return benefitService.updateBenefit(benefitId, request);
    }
}
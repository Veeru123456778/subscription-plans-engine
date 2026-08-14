package com.firstclub.membership.tier.controller;

import com.firstclub.membership.tier.dto.CreateTierRequest;
import com.firstclub.membership.tier.dto.TierResponse;
import com.firstclub.membership.tier.dto.UpdateTierRequest;
import com.firstclub.membership.tier.service.TierService;
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
@RequestMapping("/api/v1/admin/tiers")
public class TierAdminController {

    private final TierService tierService;

    public TierAdminController(TierService tierService) {
        this.tierService = tierService;
    }

    @PostMapping
    public ResponseEntity<TierResponse> createTier(@Valid @RequestBody CreateTierRequest request) {
        TierResponse created = tierService.createTier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{tierId}")
    public TierResponse updateTier(@PathVariable UUID tierId, @Valid @RequestBody UpdateTierRequest request) {
        return tierService.updateTier(tierId, request);
    }
}

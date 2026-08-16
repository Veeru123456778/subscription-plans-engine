package com.firstclub.membership.membership.controller;

import com.firstclub.membership.membership.dto.ChangeMembershipPlanRequest;
import com.firstclub.membership.membership.dto.MembershipBenefitsResponse;
import com.firstclub.membership.membership.dto.MembershipResponse;
import com.firstclub.membership.membership.dto.SubscribeRequest;
import com.firstclub.membership.membership.dto.TierUpgradeRequest;
import com.firstclub.membership.membership.service.MembershipService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping
    public MembershipResponse getActiveMembership(
            @RequestParam UUID userId
    ) {

        return membershipService.getActiveMembership(
                userId
        );
    }

    @PostMapping("/subscribe")
    public ResponseEntity<MembershipResponse> subscribe(
            @Valid @RequestBody SubscribeRequest request
    ) {

        MembershipResponse response =
                membershipService.subscribe(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping("/{membershipId}/plan")
    public MembershipResponse changePlan(
            @PathVariable UUID membershipId,
            @Valid @RequestBody ChangeMembershipPlanRequest request
    ) {

        return membershipService.changePlan(
                membershipId,
                request
        );
    }

    @PostMapping("/{membershipId}/tier-upgrade")
    public MembershipResponse upgradeTier(
            @PathVariable UUID membershipId,
            @Valid @RequestBody TierUpgradeRequest request
    ) {

        return membershipService.upgradeTier(
                membershipId,
                request
        );
    }

    @GetMapping("/{membershipId}/benefits")
    public MembershipBenefitsResponse getEffectiveBenefits(
            @PathVariable UUID membershipId
    ) {

        return membershipService.getEffectiveBenefits(
                membershipId
        );
    }

    @PostMapping("/{membershipId}/cancel")
    public MembershipResponse cancel(
            @PathVariable UUID membershipId
    ) {

        return membershipService.cancel(
                membershipId
        );
    }
}
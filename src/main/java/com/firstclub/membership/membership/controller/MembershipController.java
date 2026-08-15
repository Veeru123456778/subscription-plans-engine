package com.firstclub.membership.membership.controller;

import com.firstclub.membership.membership.dto.MembershipResponse;
import com.firstclub.membership.membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping
    public MembershipResponse getActiveMembership(
            UUID userId
    ) {
        return membershipService.getActiveMembership(userId);
    }
}
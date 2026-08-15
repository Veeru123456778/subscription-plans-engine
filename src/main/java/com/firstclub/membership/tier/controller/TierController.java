package com.firstclub.membership.tier.controller;

import com.firstclub.membership.tier.dto.TierResponse;
import com.firstclub.membership.tier.service.TierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tiers")
@RequiredArgsConstructor
public class TierController {

    private final TierService tierService;

    @GetMapping
    public List<TierResponse> getActiveTiers() {
        return tierService.getActiveTiers();
    }
}
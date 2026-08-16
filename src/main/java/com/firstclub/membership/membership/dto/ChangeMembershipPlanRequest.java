package com.firstclub.membership.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChangeMembershipPlanRequest {

    @NotNull
    private UUID planId;

    @NotNull
    private UUID planPriceId;
}
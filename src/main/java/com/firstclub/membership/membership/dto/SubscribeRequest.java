package com.firstclub.membership.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SubscribeRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID planId;

    @NotNull
    private UUID planPriceId;
}
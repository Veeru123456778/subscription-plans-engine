package com.firstclub.membership.membership.dto;

import com.firstclub.membership.membership.entity.MembershipStatus;
import com.firstclub.membership.membership.entity.TierSource;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MembershipResponse {

    private UUID id;

    private UUID userId;

    private UUID planId;

    private UUID planPriceId;

    private UUID currentTierId;

    private TierSource tierSource;

    private MembershipStatus status;

    private Instant startDate;

    private Instant expiryDate;
}
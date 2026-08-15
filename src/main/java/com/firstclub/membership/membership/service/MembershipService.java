package com.firstclub.membership.membership.service;

import com.firstclub.membership.membership.dto.MembershipResponse;

import java.util.UUID;

public interface MembershipService {

    MembershipResponse getActiveMembership(
            UUID userId
    );
}
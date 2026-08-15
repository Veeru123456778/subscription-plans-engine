package com.firstclub.membership.membership.service.impl;

import com.firstclub.membership.membership.dto.MembershipResponse;
import com.firstclub.membership.membership.entity.MembershipStatus;
import com.firstclub.membership.membership.mapper.MembershipMapper;
import com.firstclub.membership.membership.repository.MembershipRepository;
import com.firstclub.membership.membership.service.MembershipService;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipServiceImpl
        implements MembershipService {

    private final MembershipRepository membershipRepository;
    private final MembershipMapper membershipMapper;

    @Override
    @Transactional(readOnly = true)
    public MembershipResponse getActiveMembership(
            UUID userId
    ) {

        return membershipRepository
                .findByUserIdAndStatus(
                        userId,
                        MembershipStatus.ACTIVE
                )
                .map(membershipMapper::toResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active membership not found for user: "
                                        + userId
                        )
                );
    }
}
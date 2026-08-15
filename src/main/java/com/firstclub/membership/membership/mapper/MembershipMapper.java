package com.firstclub.membership.membership.mapper;

import com.firstclub.membership.membership.dto.MembershipResponse;
import com.firstclub.membership.membership.entity.Membership;
import org.springframework.stereotype.Component;

@Component
public class MembershipMapper {

    public MembershipResponse toResponse(
            Membership membership
    ) {
        return new MembershipResponse(
                membership.getId(),
                membership.getUserId(),
                membership.getPlan().getId(),
                membership.getPlanPrice().getId(),
                membership.getCurrentTier() != null
                        ? membership.getCurrentTier().getId()
                        : null,
                membership.getTierSource(),
                membership.getStatus(),
                membership.getStartDate(),
                membership.getExpiryDate()
        );
    }
}
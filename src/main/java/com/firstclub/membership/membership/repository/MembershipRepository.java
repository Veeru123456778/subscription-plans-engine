package com.firstclub.membership.membership.repository;

import com.firstclub.membership.membership.entity.Membership;
import com.firstclub.membership.membership.entity.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository
        extends JpaRepository<Membership, UUID> {

    Optional<Membership> findByUserIdAndStatus(
            UUID userId,
            MembershipStatus status
    );

    boolean existsByUserIdAndStatus(
            UUID userId,
            MembershipStatus status
    );
}
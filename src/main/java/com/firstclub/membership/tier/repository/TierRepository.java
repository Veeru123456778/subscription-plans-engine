package com.firstclub.membership.tier.repository;

import com.firstclub.membership.tier.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TierRepository extends JpaRepository<Tier, UUID> {

    List<Tier> findByActiveTrueOrderByRankDesc();

    List<Tier> findByPlanIdAndActiveTrueOrderByRankDesc(
            UUID planId
    );

    boolean existsByPlanIdAndNameIgnoreCaseAndActiveTrue(
            UUID planId,
            String name
    );

    boolean existsByPlanIdAndRankAndActiveTrue(
            UUID planId,
            Integer rank
    );

    boolean existsByPlanIdAndNameIgnoreCaseAndActiveTrueAndIdNot(
            UUID planId,
            String name,
            UUID id
    );

    boolean existsByPlanIdAndRankAndActiveTrueAndIdNot(
            UUID planId,
            Integer rank,
            UUID id
    );
}
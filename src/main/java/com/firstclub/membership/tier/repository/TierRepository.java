package com.firstclub.membership.tier.repository;

import com.firstclub.membership.tier.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TierRepository extends JpaRepository<Tier, UUID> {

    List<Tier> findByActiveTrueOrderByRankDesc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByRank(Integer rank);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID id
    );

    boolean existsByRankAndIdNot(
            Integer rank,
            UUID id
    );
}
package com.firstclub.membership.tier.repository;

import com.firstclub.membership.tier.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TierRepository extends JpaRepository<Tier, UUID> {

    // "OrderByRankDesc" is parsed from the method name itself — Spring Data
    // turns this into "... WHERE active = true ORDER BY rank DESC" with no
    // query written by hand. This ordering matches exactly what
    // computeTierFromCriteria() (tech-spec §4.2) needs: highest tier first.
    List<Tier> findByActiveTrueOrderByRankDesc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByRank(Integer rank);
}

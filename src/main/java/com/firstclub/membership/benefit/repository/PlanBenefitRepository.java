package com.firstclub.membership.benefit.repository;

import com.firstclub.membership.benefit.entity.PlanBenefit;
import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.tier.entity.Tier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanBenefitRepository
        extends JpaRepository<PlanBenefit, UUID> {

    List<PlanBenefit> findByPlanAndActiveTrue(
            Plan plan
    );

    List<PlanBenefit> findByPlanAndTierAndActiveTrue(
            Plan plan,
            Tier tier
    );

    boolean existsByPlanAndTierAndType(
            Plan plan,
            Tier tier,
            String type
    );

    boolean existsByPlanAndTypeAndTierIsNull(
            Plan plan,
            String type
    );

    boolean existsByPlanAndTierAndTypeAndIdNot(
            Plan plan,
            Tier tier,
            String type,
            UUID id
    );

    boolean existsByPlanAndTypeAndTierIsNullAndIdNot(
            Plan plan,
            String type,
            UUID id
    );
}
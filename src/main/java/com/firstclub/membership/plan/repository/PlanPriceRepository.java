package com.firstclub.membership.plan.repository;

import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.plan.entity.PlanPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanPriceRepository
        extends JpaRepository<PlanPrice, UUID> {

    List<PlanPrice> findByPlanAndActiveTrue(Plan plan);
}
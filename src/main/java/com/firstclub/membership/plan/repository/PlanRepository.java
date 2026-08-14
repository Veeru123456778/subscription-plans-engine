package com.firstclub.membership.plan.repository;

import com.firstclub.membership.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Extending JpaRepository gives us save(), findById(), findAll(), delete(), etc.
// for free — Spring generates the implementation at startup, we never write it.
// Declaring a method here that follows Spring Data's naming convention
// (findBy + field name) is enough for Spring to generate the query itself,
// no SQL or JPQL needed.
public interface PlanRepository extends JpaRepository<Plan, UUID> {

    List<Plan> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);
}

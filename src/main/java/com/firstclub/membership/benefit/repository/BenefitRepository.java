package com.firstclub.membership.benefit.repository;

import com.firstclub.membership.benefit.entity.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BenefitRepository extends JpaRepository<Benefit, UUID> {

}
package com.firstclub.membership.plan.service.impl;

import com.firstclub.membership.common.exception.ConflictException;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.plan.dto.CreatePlanRequest;
import com.firstclub.membership.plan.dto.PlanResponse;
import com.firstclub.membership.plan.dto.UpdatePlanRequest;
import com.firstclub.membership.plan.entity.Plan;
import com.firstclub.membership.plan.mapper.PlanMapper;
import com.firstclub.membership.plan.repository.PlanRepository;
import com.firstclub.membership.plan.service.PlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    // Constructor injection, not @Autowired on fields: Spring still wires this up automatically (a class with exactly one constructor doesn't even need @Autowired on it), but this way the class can never exist in an incomplete state — there's no way to construct a PlanServiceImpl without its dependencies. It's also what makes plain unit testing possible, since a test can call "new PlanServiceImpl(mockRepo, mockMapper)" directly.

    public PlanServiceImpl(PlanRepository planRepository, PlanMapper planMapper) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
    }

    @Override
    public List<PlanResponse> getActivePlans() {
        return planRepository.findByActiveTrue().stream()
                .map(planMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        if (planRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("A plan named '" + request.getName() + "' already exists");
        }

        Plan plan = new Plan(
                request.getName(),
                request.getDurationDays(),
                request.getPrice(),
                request.getCurrency()
        );

        Plan saved = planRepository.save(plan);
        return planMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PlanResponse updatePlan(UUID planId, UpdatePlanRequest request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        // Only overwrite fields the caller actually sent — a PATCH shouldn't force every field to be resupplied. No explicit save() call needed below: @Transactional means Hibernate flushes these field changes to the DB automatically once this method returns (dirty checking).

        if (request.getName() != null) {
            plan.setName(request.getName());
        }
        if (request.getDurationDays() != null) {
            plan.setDurationDays(request.getDurationDays());
        }
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getCurrency() != null) {
            plan.setCurrency(request.getCurrency());
        }
        if (request.getActive() != null) {
            plan.setActive(request.getActive());
        }

        return planMapper.toResponse(plan);
    }

    @Override
    @Transactional
    public void disablePlan(UUID planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        // A soft delete, not repository.delete(plan.getId()): per requirements.md FR-2, disabling a plan must not affect members already subscribed to it — an existing Membership's plan_id foreign key would break if we actually removed the row.
        
        plan.setActive(false);
    }
}

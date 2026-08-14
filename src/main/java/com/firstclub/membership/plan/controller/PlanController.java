package com.firstclub.membership.plan.controller;

import com.firstclub.membership.plan.dto.PlanResponse;
import com.firstclub.membership.plan.service.PlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// @RestController = @Controller + @ResponseBody: every method's return value
// is written straight into the HTTP response body as JSON, instead of being
// treated as the name of an HTML view template.

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public List<PlanResponse> getActivePlans() {
        return planService.getActivePlans();
    }
}

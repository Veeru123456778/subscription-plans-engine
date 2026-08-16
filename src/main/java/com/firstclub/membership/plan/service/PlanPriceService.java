package com.firstclub.membership.plan.service;

import com.firstclub.membership.plan.dto.CreatePlanPriceRequest;
import com.firstclub.membership.plan.dto.PlanPriceResponse;
import com.firstclub.membership.plan.dto.UpdatePlanPriceRequest;
import com.firstclub.membership.plan.entity.PlanPrice;

import java.util.UUID;

public interface PlanPriceService {

    PlanPriceResponse createPrice(
            UUID planId,
            CreatePlanPriceRequest request
    );

    PlanPriceResponse updatePrice(
            UUID planId,
            UUID priceId,
            UpdatePlanPriceRequest request
    );

    void disablePrice(
            UUID planId,
            UUID priceId
    );

    PlanPrice getActivePrice(
            UUID planPriceId,
            UUID planId
    );
}
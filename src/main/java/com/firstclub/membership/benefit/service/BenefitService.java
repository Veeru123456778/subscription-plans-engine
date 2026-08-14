package com.firstclub.membership.benefit.service;

import com.firstclub.membership.benefit.dto.BenefitResponse;
import com.firstclub.membership.benefit.dto.CreateBenefitRequest;
import com.firstclub.membership.benefit.dto.UpdateBenefitRequest;

import java.util.UUID;

public interface BenefitService {

    BenefitResponse createBenefit(CreateBenefitRequest request);

    BenefitResponse updateBenefit(UUID benefitId, UpdateBenefitRequest request);
}
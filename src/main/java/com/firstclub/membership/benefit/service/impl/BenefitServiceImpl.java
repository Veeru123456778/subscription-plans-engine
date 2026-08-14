package com.firstclub.membership.benefit.service.impl;

import com.firstclub.membership.benefit.dto.BenefitResponse;
import com.firstclub.membership.benefit.dto.CreateBenefitRequest;
import com.firstclub.membership.benefit.dto.UpdateBenefitRequest;
import com.firstclub.membership.benefit.entity.Benefit;
import com.firstclub.membership.benefit.mapper.BenefitMapper;
import com.firstclub.membership.benefit.repository.BenefitRepository;
import com.firstclub.membership.benefit.service.BenefitService;
import com.firstclub.membership.benefit.validation.BenefitTypeValidator;
import com.firstclub.membership.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BenefitServiceImpl implements BenefitService {

    private final BenefitRepository benefitRepository;
    private final BenefitMapper benefitMapper;
    private final BenefitTypeValidator benefitTypeValidator;

    public BenefitServiceImpl(
            BenefitRepository benefitRepository,
            BenefitMapper benefitMapper, BenefitTypeValidator benefitTypeValidator) {
        this.benefitRepository = benefitRepository;
        this.benefitMapper = benefitMapper;
        this.benefitTypeValidator = benefitTypeValidator;
    }

    @Override
    @Transactional
    public BenefitResponse createBenefit(CreateBenefitRequest request) {

        benefitTypeValidator.validate(request.getType());

        Benefit benefit = new Benefit(
                request.getType(),
                request.getValue(),
                request.getScope()
        );

        Benefit saved = benefitRepository.save(benefit);

        return benefitMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BenefitResponse updateBenefit(
            UUID benefitId,
            UpdateBenefitRequest request) {
        

        Benefit benefit = benefitRepository.findById(benefitId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Benefit not found: " + benefitId));

         if (request.getType() != null) {
            benefitTypeValidator.validate(request.getType());
            benefit.setType(request.getType());
        }


        if (request.getValue() != null) {
            benefit.setValue(request.getValue());
        }

        if (request.getScope() != null) {
            benefit.setScope(request.getScope());
        }

        if (request.getActive() != null) {
            benefit.setActive(request.getActive());
        }

        return benefitMapper.toResponse(benefit);
    }
}
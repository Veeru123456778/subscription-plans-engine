package com.firstclub.membership.benefit.validation;

import com.firstclub.membership.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class BenefitTypeValidator {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "FREE_DELIVERY",
            "DISCOUNT",
            "EARLY_ACCESS",
            "PRIORITY_SUPPORT"
    );

    public void validate(String type) {

        if (type == null || !SUPPORTED_TYPES.contains(type)) {
            throw new BadRequestException(
                    "Unsupported benefit type: " + type
            );
        }
    }
}
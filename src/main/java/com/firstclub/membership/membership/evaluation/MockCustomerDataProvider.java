package com.firstclub.membership.membership.evaluation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class MockCustomerDataProvider {

    private final Map<UUID, TierEvaluationContext> mockCustomers =
            Map.of(
                    UUID.fromString("00000000-0000-0000-0000-000000000001"),
                    new TierEvaluationContext(
                            UUID.fromString(
                                    "00000000-0000-0000-0000-000000000001"
                            ),
                            5,
                            new BigDecimal("20000"),
                            Set.of("REGULAR")
                    ),

                    UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    new TierEvaluationContext(
                            UUID.fromString(
                                    "00000000-0000-0000-0000-000000000002"
                            ),
                            12,
                            new BigDecimal("65000"),
                            Set.of("PREMIUM", "LOYAL")
                    ),

                    UUID.fromString("00000000-0000-0000-0000-000000000003"),
                    new TierEvaluationContext(
                            UUID.fromString(
                                    "00000000-0000-0000-0000-000000000003"
                            ),
                            25,
                            new BigDecimal("150000"),
                            Set.of("PREMIUM", "LOYAL", "VIP")
                    )
            );

    public TierEvaluationContext getCustomerData(
            UUID userId
    ) {

        return mockCustomers.getOrDefault(
                userId,
                new TierEvaluationContext(
                        userId,
                        0,
                        BigDecimal.ZERO,
                        Set.of()
                )
        );
    }
}
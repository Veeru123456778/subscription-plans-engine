package com.firstclub.membership.membership.evaluation;

import com.firstclub.membership.tier.entity.Tier;
import com.firstclub.membership.tier.repository.TierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TierAssignmentService {

    private final TierRepository tierRepository;
    private final MockCustomerDataProvider customerDataProvider;
    private final TierEligibilityEvaluator tierEligibilityEvaluator;

    @Transactional(readOnly = true)
    public Optional<Tier> determineInitialTier(
            UUID userId
    ) {

        TierEvaluationContext context =
                customerDataProvider.getCustomerData(userId);

        return tierRepository
                .findByActiveTrueOrderByRankDesc()
                .stream()
                .filter(tier ->
                        tierEligibilityEvaluator.qualifies(
                                tier,
                                context
                        )
                )
                .max(Comparator.comparing(Tier::getRank));
    }
}
package com.firstclub.membership.membership.evaluation;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.firstclub.membership.tier.entity.Tier;
import org.springframework.stereotype.Component;
import java.util.Map;


@Component
public class TierEligibilityEvaluator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean qualifies(
            Tier tier,
            TierEvaluationContext context
    ) {

         Map<String, Object> eligibilityMap = tier.getEligibility();

        if (eligibilityMap == null || eligibilityMap.isEmpty()) {

            return true;
        }

        try {
            JsonNode eligibility = objectMapper.valueToTree(eligibilityMap);

            if (eligibility == null || eligibility.isNull()) {
                return true;
            }

            String matchMode =
                    eligibility
                            .path("matchMode")
                            .asText("ALL");

            JsonNode rules =
                    eligibility.path("rules");

            if (!rules.isArray() || rules.isEmpty()) {
                return true;
            }

            if ("ANY".equalsIgnoreCase(matchMode)) {
                return evaluateAny(rules, context);
            }

            return evaluateAll(rules, context);

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                    "Invalid eligibility configuration for tier: "
                            + tier.getName(),
                    exception
            );
        }
    }

    private boolean evaluateAll(
            JsonNode rules,
            TierEvaluationContext context
    ) {

        for (JsonNode rule : rules) {

            if (!evaluateRule(rule, context)) {
                return false;
            }
        }

        return true;
    }

    private boolean evaluateAny(
            JsonNode rules,
            TierEvaluationContext context
    ) {

        for (JsonNode rule : rules) {

            if (evaluateRule(rule, context)) {
                return true;
            }
        }

        return false;
    }

    private boolean evaluateRule(
            JsonNode rule,
            TierEvaluationContext context
    ) {

        String type =
                rule.path("type").asText();

        return switch (type) {

            case "MIN_ORDER_COUNT" ->
                    context.getOrderCount()
                            >= rule.path("value").asInt();

            case "MIN_MONTHLY_ORDER_VALUE" ->
                    context.getMonthlyOrderValue()
                            .compareTo(
                                    rule.path("value")
                                            .decimalValue()
                            ) >= 0;

            case "COHORT_TAG" ->
                    context.getCohortTags()
                            .contains(
                                    rule.path("value").asText()
                            );

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported tier eligibility rule: "
                                    + type
                    );
        };
    }
}
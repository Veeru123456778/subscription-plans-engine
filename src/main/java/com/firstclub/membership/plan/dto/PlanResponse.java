package com.firstclub.membership.plan.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// A DTO, not the entity, is what controllers return. Keeping these separate
// means our public API shape can stay stable even if the Plan entity's
// internal fields change — and it stops JPA-managed objects (with their lazy
// proxies) from leaking into JSON serialization.



public record PlanResponse(
        UUID id,
        String name,
        Integer rank,
        BigDecimal consecutiveTierUpgradePrice,
        List<PlanPriceResponse> prices
) {
}
CREATE TABLE tier (
    id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                      VARCHAR(50)     NOT NULL UNIQUE,
    rank                      INTEGER         NOT NULL UNIQUE,
    min_order_count           INTEGER,
    min_order_value_monthly   NUMERIC(10, 2),
    cohort_tags               TEXT[],
    criteria_match_mode       VARCHAR(10)     NOT NULL DEFAULT 'ANY',
    is_active                 BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at                TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ     NOT NULL DEFAULT now(),

    CONSTRAINT chk_criteria_match_mode CHECK (criteria_match_mode IN ('ANY', 'ALL'))
);

CREATE INDEX idx_tier_is_active ON tier (is_active);

-- resolveTier() (tech-spec §4) always evaluates tiers ordered by rank descending,
-- so an index on rank supports that scan directly instead of a full table sort.
CREATE INDEX idx_tier_rank ON tier (rank);

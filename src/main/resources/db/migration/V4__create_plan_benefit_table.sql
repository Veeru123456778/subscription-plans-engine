CREATE TABLE plan_benefit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    plan_id UUID NOT NULL
        REFERENCES plan(id),

    tier_id UUID
        REFERENCES tier(id),

    type VARCHAR(50) NOT NULL,

    value NUMERIC(19, 2),

    discount_type VARCHAR(20),

    eligibility JSONB,

    monthly_limit INTEGER,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_plan_benefit_plan_id
    ON plan_benefit (plan_id);

CREATE INDEX idx_plan_benefit_tier_id
    ON plan_benefit (tier_id);

CREATE INDEX idx_plan_benefit_active
    ON plan_benefit (is_active);
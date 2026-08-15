CREATE TABLE tier (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    rank INTEGER NOT NULL,
    eligibility JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_tier_plan
        FOREIGN KEY (plan_id)
        REFERENCES plan(id)
);
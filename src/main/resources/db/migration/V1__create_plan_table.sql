CREATE TABLE plan (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(50) NOT NULL UNIQUE,

    rank INTEGER NOT NULL UNIQUE,

    consecutive_tier_upgrade_price NUMERIC(10, 2) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_plan_is_active
    ON plan (is_active);
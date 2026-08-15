CREATE TABLE plan_price (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    plan_id UUID NOT NULL
        REFERENCES plan(id),

    billing_period VARCHAR(20) NOT NULL,

    duration_days INTEGER NOT NULL,

    price NUMERIC(10, 2) NOT NULL,

    currency VARCHAR(3) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uk_plan_price_plan_period
        UNIQUE (plan_id, billing_period)
);
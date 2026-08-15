CREATE TABLE membership (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID NOT NULL,

    plan_id UUID NOT NULL
        REFERENCES plan(id),

    plan_price_id UUID NOT NULL
        REFERENCES plan_price(id),

    current_tier_id UUID
        REFERENCES tier(id),

    tier_source VARCHAR(30) NOT NULL,

    status VARCHAR(20) NOT NULL,

    start_date TIMESTAMPTZ NOT NULL,

    expiry_date TIMESTAMPTZ NOT NULL,

    version BIGINT NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_membership_user_id
    ON membership (user_id);

CREATE INDEX idx_membership_status
    ON membership (status);

CREATE INDEX idx_membership_expiry_date
    ON membership (expiry_date);

CREATE INDEX idx_membership_plan_id
    ON membership (plan_id);

CREATE INDEX idx_membership_current_tier_id
    ON membership (current_tier_id);
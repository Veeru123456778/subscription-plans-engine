CREATE TABLE tier (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(50) NOT NULL UNIQUE,
    rank            INTEGER NOT NULL UNIQUE,
    eligibility     JSONB ,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tier_is_active
    ON tier (is_active);

CREATE INDEX idx_tier_rank
    ON tier (rank);
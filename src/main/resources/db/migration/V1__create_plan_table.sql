CREATE TABLE plan (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           VARCHAR(50)     NOT NULL UNIQUE,
    duration_days  INTEGER         NOT NULL,
    price          NUMERIC(10, 2)  NOT NULL,
    currency       VARCHAR(3)      NOT NULL,
    is_active      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- Admin-facing listing (GET /plans) always filters on is_active; index supports that scan.
CREATE INDEX idx_plan_is_active ON plan (is_active);

CREATE TABLE "branch" (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    city VARCHAR(80),
    state VARCHAR(2)
);

CREATE TABLE grain_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(60) NOT NULL UNIQUE,
    purchase_price_per_ton NUMERIC(19, 2) NOT NULL,
    available_quantity_tons NUMERIC(19, 3) NOT NULL DEFAULT 0
);

CREATE TABLE truck (
    id BIGSERIAL PRIMARY KEY,
    plate VARCHAR(10) NOT NULL UNIQUE,
    tare_weight_kg NUMERIC(19, 2) NOT NULL,
    cargo_capacity_tons NUMERIC(19, 3)
);

CREATE TABLE scale (
    code VARCHAR(30) PRIMARY KEY,
    branch_id BIGINT NOT NULL REFERENCES branch (id),
    location VARCHAR(120),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE transport_transaction (
    id BIGSERIAL PRIMARY KEY,
    truck_id BIGINT NOT NULL REFERENCES truck (id),
    grain_type_id BIGINT NOT NULL REFERENCES grain_type (id),
    origin_branch_id BIGINT NOT NULL REFERENCES branch (id),
    scale_code VARCHAR(30) REFERENCES scale (code),
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    weighing_started_at TIMESTAMPTZ,
    weighing_completed_at TIMESTAMPTZ,
    gross_weight_kg NUMERIC(19, 2),
    tare_weight_kg NUMERIC(19, 2),
    net_weight_kg NUMERIC(19, 2),
    purchase_price_snapshot NUMERIC(19, 2),
    cargo_cost NUMERIC(19, 2)
);

CREATE INDEX idx_transport_transaction_truck_status ON transport_transaction (truck_id, status);

-- Enforces at the database level that a truck can have at most one open
-- (not yet completed/cancelled) transport transaction at a time.
CREATE UNIQUE INDEX uq_truck_active_transaction ON transport_transaction (truck_id)
    WHERE status IN ('STARTED', 'WEIGHING');

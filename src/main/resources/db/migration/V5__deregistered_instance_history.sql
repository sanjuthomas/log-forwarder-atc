CREATE TABLE deregistered_instance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instance_id     UUID         NOT NULL,
    hostname        VARCHAR(255) NOT NULL,
    process_id      BIGINT       NOT NULL,
    port            INTEGER      NOT NULL,
    registered_at   TIMESTAMPTZ  NOT NULL,
    deregistered_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_deregistered_instance_time ON deregistered_instance (deregistered_at DESC);

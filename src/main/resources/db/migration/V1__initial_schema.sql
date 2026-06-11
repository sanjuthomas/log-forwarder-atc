CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE log_forwarder_instance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hostname        VARCHAR(255) NOT NULL,
    pid             BIGINT       NOT NULL,
    start_time      TIMESTAMPTZ  NOT NULL,
    health_port     INTEGER      NOT NULL,
    ready_port      INTEGER      NOT NULL,
    metrics_port    INTEGER      NOT NULL,
    registered_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_seen_at    TIMESTAMPTZ,
    reachability    VARCHAR(32)  NOT NULL DEFAULT 'UNKNOWN',
    CONSTRAINT uq_instance_hostname_pid UNIQUE (hostname, pid)
);

CREATE INDEX idx_instance_hostname ON log_forwarder_instance (hostname);
CREATE INDEX idx_instance_reachability ON log_forwarder_instance (reachability);

CREATE TABLE instance_metrics_snapshot (
    time                TIMESTAMPTZ  NOT NULL,
    instance_id         UUID         NOT NULL REFERENCES log_forwarder_instance (id) ON DELETE CASCADE,
    health_up           BOOLEAN      NOT NULL,
    ready_up            BOOLEAN      NOT NULL,
    files_monitored     BIGINT,
    events_processed    BIGINT,
    bytes_read          BIGINT,
    poll_error          VARCHAR(512),
    PRIMARY KEY (instance_id, time)
);

SELECT create_hypertable('instance_metrics_snapshot', 'time', if_not_exists => TRUE);

CREATE INDEX idx_metrics_instance_time ON instance_metrics_snapshot (instance_id, time DESC);

CREATE TABLE fleet_counter (
    name  VARCHAR(64) PRIMARY KEY,
    value BIGINT NOT NULL DEFAULT 0
);

INSERT INTO fleet_counter (name, value) VALUES ('deregistered_total', 0);

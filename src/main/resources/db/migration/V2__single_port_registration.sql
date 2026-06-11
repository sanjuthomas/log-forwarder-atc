ALTER TABLE log_forwarder_instance RENAME COLUMN pid TO process_id;
ALTER TABLE log_forwarder_instance RENAME COLUMN start_time TO timestamp;

ALTER TABLE log_forwarder_instance ADD COLUMN port INTEGER;
UPDATE log_forwarder_instance SET port = health_port;
ALTER TABLE log_forwarder_instance ALTER COLUMN port SET NOT NULL;

ALTER TABLE log_forwarder_instance DROP COLUMN health_port;
ALTER TABLE log_forwarder_instance DROP COLUMN ready_port;
ALTER TABLE log_forwarder_instance DROP COLUMN metrics_port;

ALTER TABLE log_forwarder_instance DROP CONSTRAINT uq_instance_hostname_pid;
ALTER TABLE log_forwarder_instance ADD CONSTRAINT uq_instance_hostname_process_id UNIQUE (hostname, process_id);

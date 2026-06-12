ALTER TABLE log_forwarder_instance DROP CONSTRAINT uq_instance_hostname_process_id;

ALTER TABLE log_forwarder_instance ADD CONSTRAINT uq_instance_hostname_port UNIQUE (hostname, port);

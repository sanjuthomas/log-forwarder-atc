ALTER TABLE instance_metrics_snapshot RENAME COLUMN files_monitored TO files_watched;
ALTER TABLE instance_metrics_snapshot RENAME COLUMN events_processed TO lines_published;
ALTER TABLE instance_metrics_snapshot RENAME COLUMN bytes_read TO lines_read;

ALTER TABLE instance_metrics_snapshot ADD COLUMN pipeline_buffer_depth BIGINT;
ALTER TABLE instance_metrics_snapshot ADD COLUMN publish_hibernating BOOLEAN;

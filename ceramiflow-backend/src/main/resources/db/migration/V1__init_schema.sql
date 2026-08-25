CREATE TABLE production_orders (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, order_code VARCHAR(40) NOT NULL UNIQUE, raw_description LONGTEXT NOT NULL,
 status VARCHAR(30) NOT NULL, priority VARCHAR(20) NOT NULL, quantity INT NOT NULL, deadline DATE NULL,
 created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL
);
CREATE TABLE production_specifications (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, order_id BIGINT NOT NULL UNIQUE, product_type VARCHAR(255) NOT NULL,
 clay_type VARCHAR(255), glaze_type VARCHAR(255), pattern_description VARCHAR(255), height_cm DOUBLE, width_cm DOUBLE,
 estimated_clay_kg DOUBLE, estimated_glaze_kg DOUBLE, firing_temperature_c INT, estimated_firing_hours DOUBLE, deadline_days INT,
 needs_review BOOLEAN NOT NULL DEFAULT TRUE, review_note VARCHAR(1000), source VARCHAR(20) NOT NULL, created_at DATETIME NOT NULL,
 CONSTRAINT fk_spec_order FOREIGN KEY(order_id) REFERENCES production_orders(id)
);
CREATE TABLE production_batches (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, version BIGINT DEFAULT 0, batch_code VARCHAR(40) NOT NULL UNIQUE, order_id BIGINT NOT NULL UNIQUE,
 status VARCHAR(30) NOT NULL, current_stage VARCHAR(30) NOT NULL, quantity INT NOT NULL, started_at DATETIME NOT NULL,
 estimated_completion_at DATETIME NULL, completed_at DATETIME NULL,
 CONSTRAINT fk_batch_order FOREIGN KEY(order_id) REFERENCES production_orders(id)
);
CREATE TABLE workflow_steps (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, batch_id BIGINT NOT NULL, step_type VARCHAR(30) NOT NULL, status VARCHAR(20) NOT NULL,
 sequence_no INT NOT NULL, started_at DATETIME NULL, completed_at DATETIME NULL, operator VARCHAR(120), notes VARCHAR(1000),
 CONSTRAINT uk_batch_step_sequence UNIQUE(batch_id,sequence_no), CONSTRAINT fk_step_batch FOREIGN KEY(batch_id) REFERENCES production_batches(id)
);
CREATE TABLE qc_inspections (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, batch_id BIGINT NOT NULL, quantity_inspected INT NOT NULL, quantity_passed INT NOT NULL,
 quantity_failed INT NOT NULL, defect_type VARCHAR(255), severity VARCHAR(20), defect_rate DOUBLE NOT NULL, decision VARCHAR(30) NOT NULL,
 notes VARCHAR(1000), created_at DATETIME NOT NULL, CONSTRAINT fk_qc_batch FOREIGN KEY(batch_id) REFERENCES production_batches(id)
);
CREATE TABLE production_logs (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, batch_id BIGINT NULL, event_type VARCHAR(50) NOT NULL, from_status VARCHAR(40), to_status VARCHAR(40),
 message VARCHAR(1000) NOT NULL, metadata LONGTEXT, created_by VARCHAR(120), created_at DATETIME NOT NULL,
 CONSTRAINT fk_log_batch FOREIGN KEY(batch_id) REFERENCES production_batches(id)
);
CREATE TABLE notifications (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, batch_id BIGINT NULL, channel VARCHAR(20) NOT NULL, severity VARCHAR(20) NOT NULL,
 message LONGTEXT NOT NULL, status VARCHAR(20) NOT NULL, attempt_count INT NOT NULL DEFAULT 0, last_error VARCHAR(1000), sent_at DATETIME NULL,
 created_at DATETIME NOT NULL, CONSTRAINT fk_notification_batch FOREIGN KEY(batch_id) REFERENCES production_batches(id)
);
CREATE INDEX idx_batch_status_stage ON production_batches(status,current_stage);
CREATE INDEX idx_log_batch_created ON production_logs(batch_id,created_at);
CREATE INDEX idx_notification_status_created ON notifications(status,created_at);

-- schema.sql - tworzenie tabel
DROP TABLE IF EXISTS recipients;
DROP TABLE IF EXISTS rules;
DROP TABLE IF EXISTS notification_templates;

CREATE TABLE notification_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL
);

CREATE TABLE rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    operator VARCHAR(50) NOT NULL,
    operand VARCHAR(255) NOT NULL,
    template_id BIGINT NOT NULL,
    FOREIGN KEY (template_id) REFERENCES notification_templates(id) ON DELETE CASCADE
);

CREATE TABLE recipients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    template_id BIGINT NOT NULL,
    FOREIGN KEY (template_id) REFERENCES notification_templates(id) ON DELETE CASCADE
);

CREATE INDEX idx_title ON notification_templates(title);
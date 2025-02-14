--liquibase formatted sql
--changeset rossn:14-create-temporary-table.sql

CREATE TABLE IF NOT EXISTS crown_court_proceeding.JOB_STATUS
(
    id SERIAL PRIMARY KEY,
    job_name VARCHAR(255) NOT NULL,
    is_running BOOLEAN NOT NULL
);




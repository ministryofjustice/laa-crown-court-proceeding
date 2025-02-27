--liquibase formatted sql
--changeset Ross Nation:14-update-empty-dead-letter-report-status-field.sql

UPDATE crown_court_proceeding.dead_letter_message
SET reporting_status = 'PENDING'
WHERE reporting_status IS NULL OR reporting_status = '';
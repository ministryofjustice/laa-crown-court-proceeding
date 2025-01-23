--liquibase formatted sql
--changeset Josh Hunt:13-add-column-dead-letter-message-table.sql
ALTER TABLE crown_court_proceeding.DEAD_LETTER_MESSAGE ADD COLUMN REPORTING_STATUS VARCHAR(200) DEFAULT 'PENDING';

--liquibase formatted sql
--changeset Karl Baker:12-dead-letter-message-table.sql
CREATE SEQUENCE IF NOT EXISTS crown_court_proceeding.DEAD_LETTER INCREMENT BY 1 START WITH 1;

CREATE TABLE IF NOT EXISTS crown_court_proceeding.DEAD_LETTER_MESSAGE
(   ID INTEGER NOT NULL DEFAULT nextval('crown_court_proceeding.DEAD_LETTER') PRIMARY KEY,
    MESSAGE JSONB,
    REASON VARCHAR(200),
    RECEIVED_TIME TIMESTAMP (6) NOT NULL
);

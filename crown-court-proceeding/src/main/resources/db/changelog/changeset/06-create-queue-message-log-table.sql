--liquibase formatted sql
--changeset muthus:06-create-queue-message-log-table.sql
CREATE TABLE IF NOT EXISTS crown_court_proceeding.QUEUE_MESSAGE_LOG
   (
    TRANSACTION_UUID VARCHAR(200) NOT NULL,
	MAAT_ID INTEGER NOT NULL,
    LAA_TRANSACTION_ID VARCHAR(100),
	TYPE VARCHAR(100),
	MESSAGE BYTEA,
	CREATED_TIME TIMESTAMP (6) NOT NULL
   );



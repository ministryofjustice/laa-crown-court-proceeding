--liquibase formatted sql
--changeset muthus:07-create-prosecution-concluded-table.sql
CREATE TABLE IF NOT EXISTS crown_court_proceeding.PROSECUTION_CONCLUDED
(	"ID" INTEGER PRIMARY KEY,
     "MAAT_ID" INTEGER,
     "HEARING_ID" VARCHAR(200),
     "CASE_DATA" BYTEA,
     "STATUS" VARCHAR(20),
     "CREATED_TIME" TIMESTAMP (6),
     "UPDATED_TIME" TIMESTAMP (6),
     "RETRY_COUNT" INTEGER DEFAULT 0
);



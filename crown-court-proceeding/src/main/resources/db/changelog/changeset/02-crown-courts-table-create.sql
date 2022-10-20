--liquibase formatted sql
--changeset venkatv:02-crown-courts-table-create.sql
CREATE TABLE IF NOT EXISTS crown_court_proceeding.crown_courts
(
    code VARCHAR(6) NOT NULL,
    description VARCHAR(100) NOT NULL,
    date_created TIMESTAMP NOT NULL,
    user_created VARCHAR(100) NOT NULL,
    date_modified TIMESTAMP,
    user_modified VARCHAR(100),
    go_live_date TIMESTAMP,
    cjs_area_code VARCHAR(2),
    ou_code VARCHAR(7),
    CONSTRAINT pk_crown_courts PRIMARY KEY (code)
);
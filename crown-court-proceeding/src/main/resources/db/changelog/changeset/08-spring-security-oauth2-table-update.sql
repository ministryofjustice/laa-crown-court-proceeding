--liquibase formatted sql
--changeset matthewh:04-spring-security-oauth2-table-update

ALTER TABLE oauth2_authorization
    ADD COLUMN authorized_scopes varchar(1000) DEFAULT NULL;


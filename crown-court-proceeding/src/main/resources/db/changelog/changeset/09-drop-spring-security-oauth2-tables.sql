--liquibase formatted sql
--changeset matthewh:05-drop-spring-security-oauth2-tables.sql

DROP TABLE IF EXISTS oauth2_authorization;
DROP TABLE IF EXISTS oauth2_registered_client;
DROP TABLE IF EXISTS oauth2_authorization_consent;


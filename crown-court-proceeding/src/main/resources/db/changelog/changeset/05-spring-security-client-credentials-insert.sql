--liquibase formatted sql
--changeset vvelpuri:05-spring-security-client-credentials-insert

INSERT INTO oauth2_registered_client(
	id, client_id, client_id_issued_at, client_secret, client_secret_expires_at, client_name, client_authentication_methods, authorization_grant_types, redirect_uris, scopes, client_settings, token_settings)
	VALUES ('51f067e9-97a7-4ee8-9d17-c7722d4ed0d0', 'maat-application-client', '2022-03-01 09:04:55.995447', '{bcrypt}$2a$12$PJJSQOVeiRlLfsBc07VCXOUea80h1.682VAdUKaxpJ9UcJYK6FINy', null, '51f067e9-97a7-4ee8-9d17-c7722d4ed0d0', 'client_secret_basic', 'client_credentials', 'http://127.0.0.1:8087/authorized', 'READ,READ_WRITE', '{"@class":"java.util.Collections$UnmodifiableMap","settings.client.require-proof-key":false,"settings.client.require-authorization-consent":false}', '{"@class":"java.util.Collections$UnmodifiableMap","settings.token.reuse-refresh-tokens":true,"settings.token.id-token-signature-algorithm":["org.springframework.security.oauth2.jose.jws.SignatureAlgorithm","RS256"],"settings.token.access-token-time-to-live":["java.time.Duration",300.000000000],"settings.token.refresh-token-time-to-live":["java.time.Duration",3600.000000000]}');
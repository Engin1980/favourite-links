CREATE TABLE users
(
    id          INT AUTO_INCREMENT NOT NULL,
    email       VARCHAR(255) NOT NULL,
    keycloak_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_keycloakid UNIQUE (keycloak_id);
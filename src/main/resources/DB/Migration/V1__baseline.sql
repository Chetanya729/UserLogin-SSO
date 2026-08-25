-- Baseline schema for the SSO project.
-- Derived from the JPA entities as of the switch away from ddl-auto: update.

CREATE TABLE users (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NULL,
    role     VARCHAR(20)  NOT NULL,
    provider VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE password_reset_token (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    token      VARCHAR(36) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expiry_at  DATETIME(6) NOT NULL,
    used       BIT(1)      NOT NULL,
    user_id    BIGINT      NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_password_reset_token_token (token),
    CONSTRAINT fk_password_reset_token_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Backs Spring Security's persistent remember-me tokens.
-- Not a JPA entity: written directly by CustomPersistentTokenRepository.
CREATE TABLE persistent_logins (
    username  VARCHAR(64) NOT NULL,
    series    VARCHAR(64) NOT NULL,
    token     VARCHAR(64) NOT NULL,
    last_used TIMESTAMP   NOT NULL,
    PRIMARY KEY (series)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE users
    ADD COLUMN two_factor_enabled BIT(1) NOT NULL DEFAULT 0;
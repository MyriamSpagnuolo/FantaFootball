BEGIN;

ALTER TABLE app_users ADD COLUMN email varchar(254);
ALTER TABLE app_users ADD COLUMN token_version integer NOT NULL DEFAULT 0;
UPDATE app_users SET email = username || '@example.local' WHERE email IS NULL;
ALTER TABLE app_users ALTER COLUMN email SET NOT NULL;
CREATE UNIQUE INDEX ux_app_users_email_lower ON app_users (lower(email));

CREATE TABLE password_reset_token (
    id uuid PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
    token_hash varchar(64) NOT NULL UNIQUE,
    created_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    used_at timestamp with time zone,
    CONSTRAINT ck_password_reset_token_expiry CHECK (expires_at > created_at)
);

CREATE INDEX ix_password_reset_token_user_id ON password_reset_token(user_id);
CREATE INDEX ix_password_reset_token_expires_at ON password_reset_token(expires_at);

COMMIT;

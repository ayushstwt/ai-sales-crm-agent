-- Add email verification, password reset columns, and SUPER_ADMIN support

ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verification_expiry TIMESTAMP WITH TIME ZONE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS password_reset_expiry TIMESTAMP WITH TIME ZONE;

-- Allow organization_id to be nullable for platform-level SUPER_ADMIN users
ALTER TABLE users ALTER COLUMN organization_id DROP NOT NULL;

-- Seed SUPER_ADMIN into user_types and roles
INSERT INTO user_types (name, description, is_active, is_deleted, created_on)
VALUES ('SUPER_ADMIN', 'Super Administrator with cross-organization platform access', TRUE, FALSE, CURRENT_TIMESTAMP);

INSERT INTO roles (name, description, is_active, is_deleted, created_on)
VALUES ('ROLE_SUPER_ADMIN', 'Super Administrator Authority', TRUE, FALSE, CURRENT_TIMESTAMP);

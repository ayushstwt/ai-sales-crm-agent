-- Initial schema migration for AI Sales CRM Agent
-- Organizations, Users, User Types, Roles, and User Roles

-- Organizations Table
CREATE TABLE IF NOT EXISTS organizations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP WITH TIME ZONE
);

-- User Types Table (Enum-backed: ORG_ADMIN, SALES_MANAGER, SALES_REP)
CREATE TABLE IF NOT EXISTS user_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP WITH TIME ZONE
);

-- Roles Table (Enum-backed: ROLE_ORG_ADMIN, ROLE_SALES_MANAGER, ROLE_SALES_REP)
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP WITH TIME ZONE
);

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    user_type_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_on TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
    CONSTRAINT fk_users_user_type FOREIGN KEY (user_type_id) REFERENCES user_types(id)
);

-- User Roles Join Table (Many-to-Many)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Seed user_types
INSERT INTO user_types (name, description, is_active, is_deleted, created_on)
VALUES 
    ('ORG_ADMIN', 'Organization Administrator with full access to organization data', TRUE, FALSE, CURRENT_TIMESTAMP),
    ('SALES_MANAGER', 'Sales Manager with visibility into team pipelines and deals', TRUE, FALSE, CURRENT_TIMESTAMP),
    ('SALES_REP', 'Sales Representative with access to assigned leads, deals, and activities', TRUE, FALSE, CURRENT_TIMESTAMP);

-- Seed roles
INSERT INTO roles (name, description, is_active, is_deleted, created_on)
VALUES 
    ('ROLE_ORG_ADMIN', 'Organization Administrator Authority', TRUE, FALSE, CURRENT_TIMESTAMP),
    ('ROLE_SALES_MANAGER', 'Sales Manager Authority', TRUE, FALSE, CURRENT_TIMESTAMP),
    ('ROLE_SALES_REP', 'Sales Representative Authority', TRUE, FALSE, CURRENT_TIMESTAMP);

-- Application Configuration Table
CREATE TABLE IF NOT EXISTS application_config (
                                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(500),
    encrypted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- Access Groups Table
CREATE TABLE IF NOT EXISTS access_groups (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             group_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Roles Table
CREATE TABLE IF NOT EXISTS roles (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Users Table
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    access_group_id BIGINT,
    enabled BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    must_change_password BOOLEAN DEFAULT FALSE,
    last_login TIMESTAMP NULL,
    password_changed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (access_group_id) REFERENCES access_groups(id)
    );

-- User Roles Junction Table
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL,
                                          PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id)
    );

-- Sites Table
CREATE TABLE IF NOT EXISTS sites (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     site_name VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(255) NOT NULL,
    port INT DEFAULT 22,
    username VARCHAR(255) NOT NULL,
    encrypted_password TEXT NOT NULL,
    target_path VARCHAR(500) DEFAULT '/',
    email_notification VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    description TEXT,
    owner_id BIGINT,
    last_tested TIMESTAMP NULL,
    last_test_result VARCHAR(500),
    ssh_key TEXT,
    known_hosts_entry TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    FOREIGN KEY (updated_by) REFERENCES users(id)
    );

-- Audit Logs Table (IRS-1075 Compliance)
CREATE TABLE IF NOT EXISTS audit_logs (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          user_id BIGINT,
                                          username VARCHAR(100) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(500),
    successful BOOLEAN DEFAULT TRUE,
    error_message TEXT,
    INDEX idx_audit_timestamp (timestamp),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action_type),
    FOREIGN KEY (user_id) REFERENCES users(id)
    );

-- Transaction Logs Table
CREATE TABLE IF NOT EXISTS transaction_logs (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                site_id BIGINT NOT NULL,
                                                user_id BIGINT,
                                                action VARCHAR(100) NOT NULL,
    file_path VARCHAR(1000),
    file_size BIGINT,
    checksum VARCHAR(255),
    success BOOLEAN DEFAULT TRUE,
    error_message TEXT,
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_ms BIGINT,
    INDEX idx_trans_timestamp (timestamp),
    INDEX idx_trans_site (site_id),
    FOREIGN KEY (site_id) REFERENCES sites(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
    );

-- Scheduled Tasks Table
CREATE TABLE IF NOT EXISTS scheduled_tasks (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               task_name VARCHAR(255) NOT NULL,
    site_id BIGINT NOT NULL,
    days_of_week VARCHAR(50),
    execution_time TIME,
    jython_script TEXT NOT NULL,
    command_line_params VARCHAR(1000),
    enabled BOOLEAN DEFAULT TRUE,
    last_execution TIMESTAMP NULL,
    last_execution_status VARCHAR(500),
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (site_id) REFERENCES sites(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
    );

-- Initial Data
INSERT INTO access_groups (group_name, description) VALUES
                                                        ('ADMIN', 'Full system administrators'),
                                                        ('USER', 'Regular users with view and test capabilities');

INSERT INTO roles (role_name, description) VALUES
                                               ('ROLE_ADMIN', 'Administrator role'),
                                               ('ROLE_USER', 'Regular user role'),
                                               ('ROLE_OWNER', 'Site owner role');

-- Default admin user (password: admin123 - must change on first login)
INSERT INTO users (username, password, email, first_name, last_name, access_group_id, must_change_password)
VALUES ('admin', '$2a$10$N.eKrThoLjvc.hO0LdY8YuDBPxyHWCa8jQ7IrZkSZwF6mMaI35Wvi',
        'admin@company.com', 'System', 'Administrator', 1, TRUE);

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);

-- Application default configuration
INSERT INTO application_config (config_key, config_value, description, encrypted) VALUES
                                                                                      ('DB_INITIALIZED', 'true', 'Database initialization flag', FALSE),
                                                                                      ('DEBUG_MODE', 'false', 'Enable debug mode', FALSE),
                                                                                      ('SESSION_TIMEOUT', '30', 'Session timeout in minutes', FALSE),
                                                                                      ('MAX_LOGIN_ATTEMPTS', '5', 'Maximum login attempts before lockout', FALSE),
                                                                                      ('PASSWORD_EXPIRY_DAYS', '90', 'Password expiry in days', FALSE);
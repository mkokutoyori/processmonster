-- ProcessMonster Banking BPM - Database Initialization Script
-- Creates default roles, permissions, and admin user
-- Password for admin: admin123 (BCrypt hash with strength 12)

-- =====================================================
-- PERMISSIONS
-- =====================================================
INSERT INTO permissions (id, name, description, resource, action) VALUES
-- User permissions
(1, 'USER_READ', 'Read user information', 'USER', 'READ'),
(2, 'USER_WRITE', 'Create and update users', 'USER', 'WRITE'),
(3, 'USER_DELETE', 'Delete users', 'USER', 'DELETE'),

-- Process permissions
(4, 'PROCESS_READ', 'View process definitions', 'PROCESS', 'READ'),
(5, 'PROCESS_WRITE', 'Create and modify processes', 'PROCESS', 'WRITE'),
(6, 'PROCESS_DELETE', 'Delete process definitions', 'PROCESS', 'DELETE'),
(7, 'PROCESS_EXECUTE', 'Start process instances', 'PROCESS', 'EXECUTE'),

-- Task permissions
(8, 'TASK_READ', 'View tasks', 'TASK', 'READ'),
(9, 'TASK_WRITE', 'Update tasks', 'TASK', 'WRITE'),
(10, 'TASK_COMPLETE', 'Complete tasks', 'TASK', 'COMPLETE'),
(11, 'TASK_ASSIGN', 'Assign tasks to users', 'TASK', 'ASSIGN'),

-- Form permissions
(12, 'FORM_READ', 'View forms', 'FORM', 'READ'),
(13, 'FORM_WRITE', 'Create and modify forms', 'FORM', 'WRITE'),
(14, 'FORM_DELETE', 'Delete forms', 'FORM', 'DELETE'),

-- Report permissions
(15, 'REPORT_READ', 'View reports', 'REPORT', 'READ'),
(16, 'REPORT_GENERATE', 'Generate reports', 'REPORT', 'GENERATE'),
(17, 'REPORT_EXPORT', 'Export reports', 'REPORT', 'EXPORT'),

-- Audit permissions
(18, 'AUDIT_READ', 'View audit logs', 'AUDIT', 'READ'),
(19, 'AUDIT_EXPORT', 'Export audit logs', 'AUDIT', 'EXPORT'),

-- Admin permissions
(20, 'ADMIN_SYSTEM', 'System administration', 'ADMIN', 'SYSTEM'),
(21, 'ADMIN_USERS', 'User administration', 'ADMIN', 'USERS'),
(22, 'ADMIN_ROLES', 'Role administration', 'ADMIN', 'ROLES');

-- =====================================================
-- ROLES
-- =====================================================
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Administrator with full system access'),
(2, 'ROLE_MANAGER', 'Manager with process and user management rights'),
(3, 'ROLE_USER', 'Standard user with basic access'),
(4, 'ROLE_ANALYST', 'Analyst with read-only access to reports'),
(5, 'ROLE_AUDITOR', 'Auditor with audit log access');

-- =====================================================
-- ROLE_PERMISSIONS (Many-to-Many)
-- =====================================================
-- ROLE_ADMIN - All permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- ROLE_MANAGER - User, Process, Task, Form, Report management
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3),  -- User management
(2, 4), (2, 5), (2, 6), (2, 7),  -- Process management
(2, 8), (2, 9), (2, 10), (2, 11),  -- Task management
(2, 12), (2, 13), (2, 14),  -- Form management
(2, 15), (2, 16), (2, 17);  -- Report management

-- ROLE_USER - Read and execute
INSERT INTO role_permissions (role_id, permission_id) VALUES
(3, 1),  -- USER_READ
(3, 4), (3, 7),  -- PROCESS_READ, PROCESS_EXECUTE
(3, 8), (3, 9), (3, 10),  -- TASK_READ, TASK_WRITE, TASK_COMPLETE
(3, 12),  -- FORM_READ
(3, 15);  -- REPORT_READ

-- ROLE_ANALYST - Read-only reports and processes
INSERT INTO role_permissions (role_id, permission_id) VALUES
(4, 1),  -- USER_READ
(4, 4),  -- PROCESS_READ
(4, 8),  -- TASK_READ
(4, 12),  -- FORM_READ
(4, 15), (4, 16), (4, 17);  -- All report permissions

-- ROLE_AUDITOR - Audit logs only
INSERT INTO role_permissions (role_id, permission_id) VALUES
(5, 18), (5, 19);  -- AUDIT_READ, AUDIT_EXPORT

-- =====================================================
-- USERS
-- =====================================================
-- Admin user: username=admin, password=admin123
-- Password hash generated with BCrypt strength 12
INSERT INTO users (id, username, email, password, first_name, last_name, enabled, deleted, failed_login_attempts, created_at, updated_at, created_by, updated_by) VALUES
(1, 'admin', 'admin@processmonster.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5tdcMMaUeCE82', 'System', 'Administrator', true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');

-- Demo manager: username=manager, password=manager123
INSERT INTO users (id, username, email, password, first_name, last_name, enabled, deleted, failed_login_attempts, created_at, updated_at, created_by, updated_by) VALUES
(2, 'manager', 'manager@processmonster.com', '$2a$12$rGTFnvVyHeDJJgAY8Vb2Y.A6RxGzFEGXHJ9OGgJUXPqTJ7WLc3xEO', 'John', 'Manager', true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');

-- Demo user: username=user, password=user123
INSERT INTO users (id, username, email, password, first_name, last_name, enabled, deleted, failed_login_attempts, created_at, updated_at, created_by, updated_by) VALUES
(3, 'user', 'user@processmonster.com', '$2a$12$8RfZEBzNZbOEzWqJbKmBjeYjIvvT7HPcC5JkZNb1Jb6BhGkFHr.Ri', 'Jane', 'User', true, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system', 'system');

-- =====================================================
-- USER_ROLES (Many-to-Many)
-- =====================================================
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),  -- admin -> ROLE_ADMIN
(2, 2),  -- manager -> ROLE_MANAGER
(3, 3);  -- user -> ROLE_USER

-- =====================================================
-- Reset sequences (for H2 compatibility)
-- =====================================================
ALTER TABLE permissions ALTER COLUMN id RESTART WITH 23;
ALTER TABLE roles ALTER COLUMN id RESTART WITH 6;
ALTER TABLE users ALTER COLUMN id RESTART WITH 4;

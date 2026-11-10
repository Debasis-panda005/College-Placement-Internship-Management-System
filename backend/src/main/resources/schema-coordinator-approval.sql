-- =====================================================================
-- COORDINATOR REGISTRATION & APPROVAL WORKFLOW SCHEMA MIGRATION
-- Project: College Placement and Internship Management System
-- Architecture: Jakarta Servlets + JSP + JDBC + MySQL
-- =====================================================================
-- Safe migration: preserves existing records, defaults status to APPROVED,
-- and provisions coordinators table with role invariants and audit trails.
-- =====================================================================

-- 1. Safely add status column to users table if not already present
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'status';

SET @alter_sql = IF(@col_exists = 0,
    'ALTER TABLE users ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT ''APPROVED'' AFTER role',
    'SELECT "Column status already exists in users"');
PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ensure all existing users are marked as APPROVED
UPDATE users SET status = 'APPROVED' WHERE status IS NULL OR status = '';

-- 2. Create coordinators table
CREATE TABLE IF NOT EXISTS coordinators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_APPROVAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_by BIGINT NULL,
    approval_date TIMESTAMP NULL,
    admin_notes TEXT NULL,
    CONSTRAINT fk_coordinators_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_coordinators_admin
        FOREIGN KEY (approved_by) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_coordinator_status
        CHECK (status IN ('PENDING_APPROVAL', 'APPROVED', 'REJECTED'))
);

-- 3. Safely migrate existing coordinators into coordinators table
INSERT INTO coordinators (user_id, employee_id, department, designation, phone, status, created_at, updated_at)
SELECT u.id,
       CONCAT('EMP-COORD-', LPAD(u.id, 3, '0')),
       'Training & Placement Cell',
       'Placement Coordinator',
       '9876543210',
       'APPROVED',
       u.created_at,
       u.created_at
FROM users u
WHERE u.role = 'COORDINATOR'
  AND u.id NOT IN (SELECT user_id FROM coordinators);

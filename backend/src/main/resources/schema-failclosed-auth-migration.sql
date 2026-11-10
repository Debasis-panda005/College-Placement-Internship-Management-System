-- =====================================================================
-- FORWARD SCHEMA MIGRATION: FAIL-CLOSED AUTHENTICATION & STATUS ENFORCEMENT
-- Project: College Placement and Internship Management System
-- Architecture: Jakarta Servlets 6.0 + JSP/JSTL + JDBC + MySQL 8+
-- =====================================================================
-- DESCRIPTION:
-- Ensures database schema integrity for fail-closed authentication.
-- 1. Validates that the 'users' table contains the 'status' column.
-- 2. Ensures existing user accounts have status set to 'APPROVED'.
-- 3. Provisions 'external_students' table with 'verification_status'.
-- 4. Provisions 'coordinators' table with approval workflow status.
-- 5. Completely idempotent and safe: will not overwrite or delete existing records.
--
-- DO NOT EXECUTE AUTOMATICALLY. Database administrators should review and run manually.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. USERS TABLE: ADD 'status' COLUMN IF MISSING
-- ---------------------------------------------------------------------
SET @col_users_status_exists = 0;
SELECT COUNT(*) INTO @col_users_status_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'status';

SET @alter_users_status_sql = IF(@col_users_status_exists = 0,
    'ALTER TABLE users ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT ''APPROVED'' AFTER role',
    'SELECT "Column status already exists in users" AS notice');
PREPARE stmt_users_status FROM @alter_users_status_sql;
EXECUTE stmt_users_status;
DEALLOCATE PREPARE stmt_users_status;

-- Normalize any NULL or empty status values to 'APPROVED' for legacy accounts
UPDATE users SET status = 'APPROVED' WHERE status IS NULL OR TRIM(status) = '';

-- ---------------------------------------------------------------------
-- 2. COORDINATORS TABLE: CREATE IF NOT EXISTS
-- ---------------------------------------------------------------------
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

-- Safely provision coordinator profile for existing coordinator accounts
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

-- ---------------------------------------------------------------------
-- 3. EXTERNAL_STUDENTS TABLE: CREATE IF NOT EXISTS
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS external_students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    college_name VARCHAR(200) NOT NULL,
    college_location VARCHAR(150) NOT NULL,
    degree VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    current_semester INT NOT NULL,
    cgpa DECIMAL(4,2) NOT NULL,
    graduation_year INT NOT NULL,
    phone VARCHAR(20) NOT NULL,
    resume_url VARCHAR(255) NULL,
    verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_by BIGINT NULL,
    approval_date TIMESTAMP NULL,
    admin_notes TEXT NULL,
    CONSTRAINT fk_ext_students_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ext_students_approver
        FOREIGN KEY (approved_by) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_ext_student_status
        CHECK (verification_status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

-- Create indexes on external_students if not present
SET @idx_status_exists = 0;
SELECT COUNT(*) INTO @idx_status_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'external_students'
  AND INDEX_NAME = 'idx_ext_students_status';

SET @idx_status_sql = IF(@idx_status_exists = 0,
    'CREATE INDEX idx_ext_students_status ON external_students(verification_status)',
    'SELECT "Index idx_ext_students_status already exists" AS notice');
PREPARE stmt_idx_status FROM @idx_status_sql;
EXECUTE stmt_idx_status;
DEALLOCATE PREPARE stmt_idx_status;

SET @idx_college_exists = 0;
SELECT COUNT(*) INTO @idx_college_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'external_students'
  AND INDEX_NAME = 'idx_ext_students_college';

SET @idx_college_sql = IF(@idx_college_exists = 0,
    'CREATE INDEX idx_ext_students_college ON external_students(college_name)',
    'SELECT "Index idx_ext_students_college already exists" AS notice');
PREPARE stmt_idx_college FROM @idx_college_sql;
EXECUTE stmt_idx_college;
DEALLOCATE PREPARE stmt_idx_college;

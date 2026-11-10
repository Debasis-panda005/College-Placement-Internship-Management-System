-- =====================================================================
-- EXTERNAL STUDENT REGISTRATION & RECRUITMENT PORTAL MIGRATION (PHASE 1)
-- Project: College Placement and Internship Management System
-- Architecture: Jakarta Servlets + JSP + JDBC + MySQL
-- =====================================================================
-- Safe migration: provisions external_students table for non-CUTM students
-- preserving existing CUTM students table without alterations.
-- =====================================================================

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

-- Optimized performance indexes for verification status and university lookups
SET @idx_status_exists = 0;
SELECT COUNT(*) INTO @idx_status_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'external_students'
  AND INDEX_NAME = 'idx_ext_students_status';

SET @idx_status_sql = IF(@idx_status_exists = 0,
    'CREATE INDEX idx_ext_students_status ON external_students(verification_status)',
    'SELECT "Index idx_ext_students_status already exists"');
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
    'SELECT "Index idx_ext_students_college already exists"');
PREPARE stmt_idx_college FROM @idx_college_sql;
EXECUTE stmt_idx_college;
DEALLOCATE PREPARE stmt_idx_college;

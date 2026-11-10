-- =====================================================================
-- JOB AUDIENCE & EXTERNAL STUDENT VISIBILITY FIX MIGRATION
-- Project: College Placement and Internship Management System
-- Architecture: Jakarta Servlets + JSP + JDBC + MySQL
-- =====================================================================
-- Non-destructive, idempotent forward migration preserving all historical data.
-- 1. Adds target_audience column to jobs table if missing.
-- 2. Configures applications table to support external student applications.
-- 3. Updates existing Job ID 104 to 'EXTERNAL' audience.
-- =====================================================================

-- 1. Add target_audience column to jobs table
SET @col_audience_exists = 0;
SELECT COUNT(*) INTO @col_audience_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'jobs'
  AND COLUMN_NAME = 'target_audience';

SET @alter_jobs_sql = IF(@col_audience_exists = 0,
    'ALTER TABLE jobs ADD COLUMN target_audience VARCHAR(20) NOT NULL DEFAULT ''INTERNAL'' AFTER eligible_departments',
    'SELECT "Column target_audience already exists in jobs"');
PREPARE stmt_jobs FROM @alter_jobs_sql;
EXECUTE stmt_jobs;
DEALLOCATE PREPARE stmt_jobs;

-- 2. Modify applications table for external applicants
-- Allow student_id to be nullable for external students
ALTER TABLE applications MODIFY COLUMN student_id BIGINT NULL;

-- Add applicant_type column
SET @col_type_exists = 0;
SELECT COUNT(*) INTO @col_type_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'applications'
  AND COLUMN_NAME = 'applicant_type';

SET @alter_app_type_sql = IF(@col_type_exists = 0,
    'ALTER TABLE applications ADD COLUMN applicant_type VARCHAR(20) NOT NULL DEFAULT ''INTERNAL'' AFTER status',
    'SELECT "Column applicant_type already exists in applications"');
PREPARE stmt_app_type FROM @alter_app_type_sql;
EXECUTE stmt_app_type;
DEALLOCATE PREPARE stmt_app_type;

-- Add external_student_id column
SET @col_ext_id_exists = 0;
SELECT COUNT(*) INTO @col_ext_id_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'applications'
  AND COLUMN_NAME = 'external_student_id';

SET @alter_app_ext_id_sql = IF(@col_ext_id_exists = 0,
    'ALTER TABLE applications ADD COLUMN external_student_id BIGINT NULL AFTER applicant_type',
    'SELECT "Column external_student_id already exists in applications"');
PREPARE stmt_app_ext_id FROM @alter_app_ext_id_sql;
EXECUTE stmt_app_ext_id;
DEALLOCATE PREPARE stmt_app_ext_id;

-- Add unique constraint for external student and job to prevent duplicate applications
SET @idx_ext_app_exists = 0;
SELECT COUNT(*) INTO @idx_ext_app_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'applications'
  AND INDEX_NAME = 'uq_applications_ext_student_job';

SET @alter_app_idx_sql = IF(@idx_ext_app_exists = 0,
    'ALTER TABLE applications ADD CONSTRAINT uq_applications_ext_student_job UNIQUE (external_student_id, job_id)',
    'SELECT "Unique index uq_applications_ext_student_job already exists"');
PREPARE stmt_app_idx FROM @alter_app_idx_sql;
EXECUTE stmt_app_idx;
DEALLOCATE PREPARE stmt_app_idx;

-- Add foreign key constraint for external_student_id
SET @fk_ext_app_exists = 0;
SELECT COUNT(*) INTO @fk_ext_app_exists
FROM information_schema.TABLE_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = DATABASE()
  AND TABLE_NAME = 'applications'
  AND CONSTRAINT_NAME = 'fk_applications_ext_student';

SET @alter_app_fk_sql = IF(@fk_ext_app_exists = 0,
    'ALTER TABLE applications ADD CONSTRAINT fk_applications_ext_student FOREIGN KEY (external_student_id) REFERENCES external_students(id) ON DELETE CASCADE ON UPDATE CASCADE',
    'SELECT "Foreign key fk_applications_ext_student already exists"');
PREPARE stmt_app_fk FROM @alter_app_fk_sql;
EXECUTE stmt_app_fk;
DEALLOCATE PREPARE stmt_app_fk;

-- 3. Explicitly update existing Job ID 104 to 'EXTERNAL'
UPDATE jobs SET target_audience = 'EXTERNAL' WHERE id = 104;

-- =====================================================================
-- JOB AUDIENCE & EXTERNAL STUDENT VISIBILITY FIX ROLLBACK SCRIPT
-- Project: College Placement and Internship Management System
-- Architecture: Jakarta Servlets + JSP + JDBC + MySQL
-- =====================================================================

-- 1. Remove foreign key and unique constraint from applications
SET @fk_ext_app_exists = 0;
SELECT COUNT(*) INTO @fk_ext_app_exists
FROM information_schema.TABLE_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = DATABASE()
  AND TABLE_NAME = 'applications'
  AND CONSTRAINT_NAME = 'fk_applications_ext_student';

SET @drop_fk_sql = IF(@fk_ext_app_exists > 0,
    'ALTER TABLE applications DROP FOREIGN KEY fk_applications_ext_student',
    'SELECT "Foreign key fk_applications_ext_student does not exist"');
PREPARE stmt_drop_fk FROM @drop_fk_sql;
EXECUTE stmt_drop_fk;
DEALLOCATE PREPARE stmt_drop_fk;

SET @idx_ext_app_exists = 0;
SELECT COUNT(*) INTO @idx_ext_app_exists
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'applications'
  AND INDEX_NAME = 'uq_applications_ext_student_job';

SET @drop_idx_sql = IF(@idx_ext_app_exists > 0,
    'ALTER TABLE applications DROP INDEX uq_applications_ext_student_job',
    'SELECT "Index uq_applications_ext_student_job does not exist"');
PREPARE stmt_drop_idx FROM @drop_idx_sql;
EXECUTE stmt_drop_idx;
DEALLOCATE PREPARE stmt_drop_idx;

SET @col_ext_id_exists = 0;
SELECT COUNT(*) INTO @col_ext_id_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'applications'
  AND COLUMN_NAME = 'external_student_id';

SET @drop_col_ext_id = IF(@col_ext_id_exists > 0,
    'ALTER TABLE applications DROP COLUMN external_student_id',
    'SELECT "Column external_student_id does not exist"');
PREPARE stmt_drop_col_ext FROM @drop_col_ext_id;
EXECUTE stmt_drop_col_ext;
DEALLOCATE PREPARE stmt_drop_col_ext;

SET @col_type_exists = 0;
SELECT COUNT(*) INTO @col_type_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'applications'
  AND COLUMN_NAME = 'applicant_type';

SET @drop_col_type = IF(@col_type_exists > 0,
    'ALTER TABLE applications DROP COLUMN applicant_type',
    'SELECT "Column applicant_type does not exist"');
PREPARE stmt_drop_type FROM @drop_col_type;
EXECUTE stmt_drop_type;
DEALLOCATE PREPARE stmt_drop_type;

-- 2. Remove target_audience from jobs
SET @col_audience_exists = 0;
SELECT COUNT(*) INTO @col_audience_exists
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'jobs'
  AND COLUMN_NAME = 'target_audience';

SET @drop_col_audience = IF(@col_audience_exists > 0,
    'ALTER TABLE jobs DROP COLUMN target_audience',
    'SELECT "Column target_audience does not exist"');
PREPARE stmt_drop_aud FROM @drop_col_audience;
EXECUTE stmt_drop_aud;
DEALLOCATE PREPARE stmt_drop_aud;

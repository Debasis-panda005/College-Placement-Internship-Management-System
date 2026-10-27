-- =====================================================================
-- REFERENCE DDL ONLY — FOR MANUAL REVIEW PRIOR TO DATABASE EXECUTION
-- =====================================================================
-- Project: College Placement and Internship Management System
-- Module: Phase 4 — Authenticated Student Applications, Eligibility & Unique Constraint
-- Target Architecture: Jakarta Servlets + JSP + JDBC + MySQL
--
-- DO NOT EXECUTE AUTOMATICALLY.
-- This script provides the standard relational schema modifications to:
-- 1. Enforce unique student-to-job applications at the database level.
-- 2. Add job eligibility constraints (deadline, min_cgpa, eligible_departments).
-- =====================================================================

-- =====================================================================
-- 1. AUDIT EXISTING DUPLICATES (PRE-MIGRATION INSPECTION)
-- =====================================================================
-- Before adding a UNIQUE constraint on (student_id, job_id), verify that
-- the current database contains no duplicate applications for the same job.
-- Run this diagnostic query first:

SELECT student_id, job_id, COUNT(*) AS application_count
FROM applications
GROUP BY student_id, job_id
HAVING COUNT(*) > 1;

-- ADVISORY:
-- If the query above returns rows, duplicate applications exist.
-- DO NOT automatically delete application history records.
-- Review duplicates with system coordinators to determine if they represent
-- erroneous re-submissions or legitimate status transitions before resolving.

-- =====================================================================
-- 2. ENFORCE UNIQUE CONSTRAINT ON APPLICATIONS
-- =====================================================================
-- Guarantees that a student can only apply to a specific job once at the
-- MySQL storage engine level (InnoDB), preventing concurrent race conditions.

ALTER TABLE applications
    ADD CONSTRAINT uq_applications_student_job UNIQUE (student_id, job_id);

-- =====================================================================
-- 3. EXTEND JOBS TABLE WITH ELIGIBILITY COLUMNS
-- =====================================================================
-- Adds optional eligibility criteria columns to the jobs table.
-- NULL values indicate unrestricted criteria (e.g. no deadline, no CGPA floor, all departments).

ALTER TABLE jobs
    ADD COLUMN deadline DATE NULL AFTER status,
    ADD COLUMN min_cgpa DECIMAL(4,2) NULL AFTER deadline,
    ADD COLUMN eligible_departments VARCHAR(255) NULL AFTER min_cgpa;

-- CGPA validity check on job requirement (0.00 to 10.00)
ALTER TABLE jobs
    ADD CONSTRAINT chk_jobs_min_cgpa CHECK (min_cgpa IS NULL OR (min_cgpa >= 0.00 AND min_cgpa <= 10.00));

-- =====================================================================
-- 4. VERIFICATION QUERIES
-- =====================================================================
-- Verify schema updates:
--   SHOW INDEX FROM applications WHERE Key_name = 'uq_applications_student_job';
--   DESCRIBE jobs;

-- =====================================================================
-- 5. ROLLBACK INSTRUCTIONS
-- =====================================================================
-- If rollback is necessary:
--   ALTER TABLE applications DROP INDEX uq_applications_student_job;
--   ALTER TABLE jobs DROP CHECK chk_jobs_min_cgpa;
--   ALTER TABLE jobs DROP COLUMN eligible_departments, DROP COLUMN min_cgpa, DROP COLUMN deadline;

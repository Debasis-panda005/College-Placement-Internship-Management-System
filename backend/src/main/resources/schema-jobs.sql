-- =====================================================================
-- REFERENCE DDL ONLY — FOR MANUAL REVIEW PRIOR TO DATABASE EXECUTION
-- =====================================================================
-- Project: College Placement and Internship Management System
-- Module: Job and Internship Management
-- Target Architecture: Jakarta Servlets + JSP + JDBC + MySQL
--
-- DO NOT EXECUTE AUTOMATICALLY.
-- This script provides the standard relational schema definition for the
-- jobs table, aligned with the existing BIGINT application keys.
-- =====================================================================

-- 1. JOBS TABLE DEFINITION
CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    company VARCHAR(150) NOT NULL,
    location VARCHAR(100) NOT NULL,
    description TEXT,
    salary VARCHAR(100),
    job_type VARCHAR(50) NOT NULL,                  -- 'Full Time', 'Part Time', 'Internship'
    status VARCHAR(50) NOT NULL DEFAULT 'Active',   -- 'Active', 'Closed'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================================
-- 2. INTEGRATION NOTES FOR EXISTING APPLICATIONS TABLE
-- =====================================================================
-- The existing 'applications' table already contains the column:
--   job_id BIGINT NOT NULL
--
-- A relational foreign key constraint can optionally be added after verifying
-- that all existing job_id records exist in the jobs table:
--
-- ALTER TABLE applications
--     ADD CONSTRAINT fk_applications_job
--     FOREIGN KEY (job_id) REFERENCES jobs(id)
--     ON DELETE RESTRICT ON UPDATE CASCADE;

-- =====================================================================
-- 3. MIGRATION ADVISORY FOR PRE-EXISTING TABLES
-- =====================================================================
-- If a 'jobs' table already exists in your local MySQL instance with INT id
-- (e.g. from the experimental feature/job-internship branch), note that
-- 'CREATE TABLE IF NOT EXISTS' will NOT alter or upgrade the existing table.
--
-- To verify existing column types:
--   DESCRIBE jobs;
--
-- If 'id' is INT, migrate to BIGINT using:
--   ALTER TABLE jobs MODIFY id BIGINT AUTO_INCREMENT;
-- =====================================================================

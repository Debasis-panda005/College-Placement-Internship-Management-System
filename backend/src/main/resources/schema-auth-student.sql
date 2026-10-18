-- =====================================================================
-- REFERENCE DDL ONLY — FOR MANUAL REVIEW PRIOR TO DATABASE EXECUTION
-- =====================================================================
-- Project: College Placement and Internship Management System
-- Module: Student Authentication Foundation & Student Management
-- Target Architecture: Jakarta Servlets + JSP + JDBC + MySQL
--
-- DO NOT EXECUTE AUTOMATICALLY.
-- This script provides the standard relational schema definition for
-- authentication (users) and student profile management (students).
-- =====================================================================

-- =====================================================================
-- 1. USERS TABLE
-- =====================================================================
-- Stores core authentication credentials and roles for all system actors:
-- STUDENT, COORDINATOR, and ADMIN.
-- Plaintext passwords are NEVER stored; password_hash holds salted BCrypt hashes.
-- Email is strictly lower-cased and unique across all accounts.
-- =====================================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('STUDENT', 'COORDINATOR', 'ADMIN')),
    -- Unique composite key allowing foreign keys to enforce role-matching invariants:
    UNIQUE KEY uq_users_id_role (id, role)
);

-- =====================================================================
-- 2. STUDENTS TABLE
-- =====================================================================
-- Contains the extended academic and placement profile for students.
-- Each student profile is strictly bound 1-to-1 to a user whose role is 'STUDENT'.
-- =====================================================================

CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    user_role VARCHAR(30) NOT NULL DEFAULT 'STUDENT',
    roll_number VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(100) NOT NULL,
    cgpa DECIMAL(4,2) NOT NULL,
    graduation_year INT NOT NULL,
    phone VARCHAR(20),
    skills TEXT,
    resume_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- CGPA constraint: Standard Indian university 10-point scale (0.00 to 10.00)
    CONSTRAINT chk_student_cgpa CHECK (cgpa >= 0.00 AND cgpa <= 10.00),

    -- Graduation year constraint
    CONSTRAINT chk_student_graduation_year CHECK (graduation_year >= 2000 AND graduation_year <= 2100),

    -- Student role constraint: ensures student profile's user_role column is 'STUDENT'
    CONSTRAINT chk_student_user_role CHECK (user_role = 'STUDENT'),

    -- Enforce foreign key relationship to users(id, role):
    -- Because user_role is constrained to 'STUDENT' and the foreign key matches (id, role),
    -- this foreign key guarantees that user_id MUST reference a user row where role = 'STUDENT'.
    CONSTRAINT fk_students_user_role
        FOREIGN KEY (user_id, user_role)
        REFERENCES users(id, role)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- =====================================================================
-- 3. INVARIANT ENFORCEMENT: "A student profile must belong to a user whose role is STUDENT"
-- =====================================================================
-- A simple single-column foreign key `FOREIGN KEY (user_id) REFERENCES users(id)` only guarantees
-- that a user row with that ID exists. It CANNOT verify the value of the `role` column in the referenced row.
--
-- In this architecture, the invariant is enforced as follows:
--
-- PRIMARY MECHANISM: Relational Schema Composite FK (Engine-Level Enforcement)
--   1. The `users` table defines `UNIQUE KEY uq_users_id_role (id, role)`.
--   2. The `students` table defines `user_role VARCHAR(30) NOT NULL DEFAULT 'STUDENT'` with
--      `CONSTRAINT chk_student_user_role CHECK (user_role = 'STUDENT')`.
--   3. The composite foreign key `CONSTRAINT fk_students_user_role
--      FOREIGN KEY (user_id, user_role) REFERENCES users(id, role)`
--      guarantees at the MySQL storage engine level that MySQL will REJECT any attempt to insert
--      or update a student profile whose referenced user does not have role = 'STUDENT'.
--   This is the authoritative, zero-overhead mechanism enforced directly by InnoDB.
--
-- APPLICATION / DAO DEFENSE: Service Layer Pre-Check (Fail-Fast Defense)
--   `StudentDAO.createStudent()` explicitly queries `SELECT role FROM users WHERE id = ?`
--   and verifies that `role = 'STUDENT'` prior to executing the insert, providing clean
--   application-level error messages before database constraints fire.
--
-- OPTIONAL / REDUNDANT: Database Trigger (Alternative Design Only)
--   NOTE: The trigger below is REDUNDANT and NOT NEEDED when the primary composite foreign
--   key (fk_students_user_role) is active. It is documented here solely as an alternative
--   pattern for legacy database environments that do not support composite foreign keys:
--
--   DELIMITER //
--   CREATE TRIGGER trg_students_check_role_before_insert
--   BEFORE INSERT ON students
--   FOR EACH ROW
--   BEGIN
--       DECLARE u_role VARCHAR(30);
--       SELECT role INTO u_role FROM users WHERE id = NEW.user_id;
--       IF u_role IS NULL OR u_role <> 'STUDENT' THEN
--           SIGNAL SQLSTATE '45000'
--           SET MESSAGE_TEXT = 'Integrity violation: Student profile can only be associated with a user having role STUDENT.';
--       END IF;
--   END //
--   DELIMITER ;
-- =====================================================================

-- =====================================================================
-- 4. LINKAGE TO EXISTING APPLICATIONS TABLE
-- =====================================================================
-- In `applications` table, the existing column:
--   student_id BIGINT NOT NULL
--
-- Once `students` table is populated in production, an optional foreign key constraint
-- can be added to complete end-to-end referential integrity:
--
-- ALTER TABLE applications
--     ADD CONSTRAINT fk_applications_student
--     FOREIGN KEY (student_id) REFERENCES students(id)
--     ON DELETE RESTRICT ON UPDATE CASCADE;
-- =====================================================================

-- =====================================================================
-- EXTERNAL STUDENT REGISTRATION SCHEMA ROLLBACK SCRIPT (PHASE 1)
-- Project: College Placement and Internship Management System
-- Architecture: Jakarta Servlets + JSP + JDBC + MySQL
-- =====================================================================
-- Safe rollback: drops external_students table.
-- Note: Does NOT modify users or students tables.
-- =====================================================================

DROP TABLE IF EXISTS external_students;

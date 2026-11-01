-- =====================================================================
-- ROLLBACK SCRIPT FOR PHASE 6 DYNAMIC INTERVIEW ROUNDS & PDF REPORTS
-- =====================================================================
-- Execute only if you need to safely revert the Phase 6 schema changes.
-- Drops tables in reverse dependency order.
-- =====================================================================

DROP TABLE IF EXISTS interview_reports;
DROP TABLE IF EXISTS interview_round_history;
DROP TABLE IF EXISTS interview_rounds;

-- Verification query:
-- SHOW TABLES LIKE 'interview_%';

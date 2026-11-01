-- =====================================================================
-- REFERENCE DDL ONLY — FOR MANUAL REVIEW PRIOR TO DATABASE EXECUTION
-- =====================================================================
-- Project: College Placement and Internship Management System
-- Module: Phase 6 — Dynamic Multi-Round Interview Management & PDF Reports
-- Target Architecture: Jakarta Servlets + JSP + JDBC + MySQL
--
-- DO NOT EXECUTE AUTOMATICALLY.
-- Wait for explicit administrator/user permission before executing this script.
-- =====================================================================

-- =====================================================================
-- 1. INTERVIEW ROUNDS TABLE
-- =====================================================================
-- Stores configurable, sequential interview rounds per application.
-- Supports any number of rounds (e.g. Aptitude, GD, Tech 1, Tech 2, HR, etc.)
-- =====================================================================

CREATE TABLE IF NOT EXISTS interview_rounds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    round_number INT NOT NULL,
    round_name VARCHAR(100) NOT NULL,
    interview_date DATE NOT NULL,
    interview_time TIME NOT NULL,
    mode VARCHAR(50) NOT NULL DEFAULT 'ONLINE',
    interviewer_name VARCHAR(150),
    interviewer_info TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    result VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    remarks TEXT,
    feedback TEXT,
    cancellation_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_ir_mode CHECK (mode IN ('ONLINE', 'OFFLINE')),
    CONSTRAINT chk_ir_status CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED')),
    CONSTRAINT chk_ir_result CHECK (result IN ('PENDING', 'PASSED', 'FAILED')),
    CONSTRAINT chk_ir_round_number CHECK (round_number >= 1),

    -- Unique round number per application
    CONSTRAINT uq_ir_app_round UNIQUE (application_id, round_number),

    -- Foreign Key to applications
    CONSTRAINT fk_ir_application
        FOREIGN KEY (application_id)
        REFERENCES applications(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE INDEX idx_ir_application ON interview_rounds(application_id);
CREATE INDEX idx_ir_status ON interview_rounds(status);
CREATE INDEX idx_ir_result ON interview_rounds(result);

-- =====================================================================
-- 2. INTERVIEW ROUND HISTORY TABLE
-- =====================================================================
-- Captures state transitions, reschedulings, completions, and cancellations
-- for full auditability and interview timeline reconstruction.
-- =====================================================================

CREATE TABLE IF NOT EXISTS interview_round_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    round_id BIGINT NOT NULL,
    application_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    action_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    previous_status VARCHAR(50),
    new_status VARCHAR(50),
    previous_result VARCHAR(50),
    new_result VARCHAR(50),
    remarks TEXT,
    performed_by VARCHAR(100),

    -- Foreign Key to interview_rounds
    CONSTRAINT fk_ir_history_round
        FOREIGN KEY (round_id)
        REFERENCES interview_rounds(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    -- Foreign Key to applications
    CONSTRAINT fk_ir_history_app
        FOREIGN KEY (application_id)
        REFERENCES applications(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE INDEX idx_ir_history_round ON interview_round_history(round_id);
CREATE INDEX idx_ir_history_app ON interview_round_history(application_id);

-- =====================================================================
-- 3. INTERVIEW REPORTS METADATA TABLE
-- =====================================================================
-- Stores metadata and summaries for authorized PDF evaluation reports
-- generated upon final interview completion (SELECTED or REJECTED).
-- =====================================================================

CREATE TABLE IF NOT EXISTS interview_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE,
    report_uuid VARCHAR(64) NOT NULL UNIQUE,
    final_decision VARCHAR(50) NOT NULL,
    decision_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_rounds INT NOT NULL DEFAULT 0,
    completed_rounds INT NOT NULL DEFAULT 0,
    passed_rounds INT NOT NULL DEFAULT 0,
    failed_rounds INT NOT NULL DEFAULT 0,
    cancelled_rounds INT NOT NULL DEFAULT 0,
    coordinator_remarks TEXT,
    overall_feedback TEXT,
    generated_by VARCHAR(100),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_ir_report_decision CHECK (final_decision IN ('SELECTED', 'REJECTED')),

    CONSTRAINT fk_ir_reports_app
        FOREIGN KEY (application_id)
        REFERENCES applications(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE INDEX idx_ir_reports_app ON interview_reports(application_id);
CREATE INDEX idx_ir_reports_uuid ON interview_reports(report_uuid);

-- =====================================================================
-- 4. VERIFICATION QUERIES
-- =====================================================================
-- DESCRIBE interview_rounds;
-- DESCRIBE interview_round_history;
-- DESCRIBE interview_reports;
-- SHOW INDEX FROM interview_rounds;
-- SHOW INDEX FROM interview_reports;

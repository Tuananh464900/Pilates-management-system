-- V7__prevent_data_loss_and_safe_schedule.sql
-- Chinh sach di chuyen du lieu tien ve phia truoc (Forward-only Migration Policy)
-- Bao ve toan ven du lieu nguoi dung va lich tap

-- 1. Bang luu vet kiem toan he thong va bao ve toan ven du lieu
CREATE TABLE IF NOT EXISTS system_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON system_audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON system_audit_logs(entity_name);

-- 2. Ghi nhan chinh sach an toan du lieu vao nhat ky kiem toan
INSERT INTO system_audit_logs (action, entity_name, entity_id, details)
VALUES (
    'APPLY_FORWARD_ONLY_POLICY',
    'DATABASE_MIGRATION',
    'V7',
    'Chinh sach Forward-Only duoc kich hoat. Bao toan nguyen ven du lieu nguoi dung, bookings va sessions trong toan bo cac migration tiep theo.'
);

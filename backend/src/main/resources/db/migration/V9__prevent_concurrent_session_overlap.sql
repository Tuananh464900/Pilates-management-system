-- V9__prevent_concurrent_session_overlap.sql
-- Bổ sung ràng buộc loại trừ khoảng thời gian (EXCLUSION constraint) tại mức PostgreSQL
-- Chống hoàn toàn tình trạng trùng lịch phòng và trùng lịch HLV khi có các request tạo lịch đồng thời

CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 1. Ràng buộc loại trừ trùng lịch phòng cho các buổi chưa bị hủy (status != 'CANCELED')
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'exclude_session_room_time_overlap'
    ) THEN
        ALTER TABLE sessions
        ADD CONSTRAINT exclude_session_room_time_overlap
        EXCLUDE USING gist (
            room_id WITH =,
            tstzrange(start_time, end_time) WITH &&
        ) WHERE (status != 'CANCELED');
    END IF;
END $$;

-- 2. Ràng buộc loại trừ trùng lịch HLV cho các buổi chưa bị hủy (status != 'CANCELED')
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'exclude_session_trainer_time_overlap'
    ) THEN
        ALTER TABLE sessions
        ADD CONSTRAINT exclude_session_trainer_time_overlap
        EXCLUDE USING gist (
            trainer_id WITH =,
            tstzrange(start_time, end_time) WITH &&
        ) WHERE (status != 'CANCELED');
    END IF;
END $$;

-- Ghi nhận audit log
INSERT INTO system_audit_logs (action, entity_name, entity_id, details)
VALUES (
    'ADD_EXCLUSION_CONSTRAINTS',
    'DATABASE_CONSTRAINT',
    'V9',
    'Đã bổ sung ràng buộc loại trừ EXCLUDE USING gist cho room_id và trainer_id trên bảng sessions chống race condition tuyệt đối.'
);


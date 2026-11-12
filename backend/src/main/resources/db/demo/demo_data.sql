-- db/demo/demo_data.sql
-- File khoi tao du lieu mau danh rieng cho moi truong Development / Demo.
-- Duoc tach roi hoan toan khoi cac migration Flyway cua Production de dam bao an toan tuyet doi.

-- 1. Khoi tao lop hoc mau neu chua co
INSERT INTO pilates_classes (id, name, description, default_capacity, level, status)
VALUES 
    (1, 'Pilates Mat Co Ban', 'Lop tap tren tham ren luyen co loi va do deo dai', 10, 'BEGINNER', 'ACTIVE'),
    (2, 'Pilates Reformer Nang Cao', 'Lop tap voi thiet bi Reformer chuyen sau', 6, 'INTERMEDIATE', 'ACTIVE'),
    (3, 'Pilates Cadillac Tri Lieu', 'Lop tap phuc hoi cot song va vung that lung', 4, 'ALL_LEVELS', 'ACTIVE'),
    (4, 'Pilates Wunda Chair Co Luc', 'Lop tap tang cuong suc manh va thang bang', 6, 'ADVANCED', 'ACTIVE')
ON CONFLICT (name) DO NOTHING;

-- 2. Khoi tao phong tap neu chua co
INSERT INTO rooms (id, name, capacity, description, status)
VALUES 
    (1, 'Studio 1 - Mat & Reformer', 12, 'Phong tap trang bi Reformer va tham cao cap', 'ACTIVE'),
    (2, 'Studio 2 - Cadillac & Chair', 8, 'Phong tap chuyen dung thiet bi tri lieu', 'ACTIVE')
ON CONFLICT (name) DO NOTHING;

-- 3. Khoi tao lich tap demo neu da co HLV trong he thong
DO $$
DECLARE
    v_trainer_id UUID;
    v_member_id UUID;
    v_membership_id BIGINT;
    v_session_id BIGINT;
BEGIN
    SELECT id INTO v_trainer_id FROM users WHERE role = 'TRAINER' LIMIT 1;
    SELECT id INTO v_member_id FROM users WHERE role = 'MEMBER' LIMIT 1;
    SELECT id INTO v_membership_id FROM member_memberships WHERE member_id = v_member_id LIMIT 1;

    IF v_trainer_id IS NOT NULL THEN
        -- Chi chen lich neu chua co lich trong khoang thoi gian nay
        IF NOT EXISTS (SELECT 1 FROM sessions WHERE start_time >= '2026-10-26 00:00:00+07' AND start_time <= '2026-11-22 23:59:59+07') THEN
            INSERT INTO sessions (class_id, trainer_id, room_id, start_time, end_time, capacity, booked_count, status)
            VALUES (1, v_trainer_id, 1, '2026-11-09 17:30:00+07', '2026-11-09 18:30:00+07', 6, 0, 'SCHEDULED');

            INSERT INTO sessions (class_id, trainer_id, room_id, start_time, end_time, capacity, booked_count, status)
            VALUES (2, v_trainer_id, 1, '2026-11-10 17:30:00+07', '2026-11-10 18:30:00+07', 8, 0, 'SCHEDULED');
        END IF;
    END IF;
END $$;


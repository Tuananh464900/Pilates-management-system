-- V6__reschedule_sessions_one_per_day.sql
-- Tach toan bo du lieu demo khoi migration production theo khuyen nghi chuan.
-- Tuyet doi KHONG chua lenh xoa (DELETE / TRUNCATE) va KHONG chen du lieu lich tap demo bang ID co dinh.
-- Migration nay bao dam an toan du lieu, thiet lap chi muc hieu nang cho sessions va bookings.

-- 1. Chi muc toi uu hoa truy van lich tap theo thoi gian va trang thai
CREATE INDEX IF NOT EXISTS idx_sessions_start_time ON sessions(start_time);
CREATE INDEX IF NOT EXISTS idx_sessions_status_start_time ON sessions(status, start_time);

-- 2. Chi muc toi uu hoa truy van danh sach dat cho theo thoi gian
CREATE INDEX IF NOT EXISTS idx_bookings_created_at ON bookings(created_at);

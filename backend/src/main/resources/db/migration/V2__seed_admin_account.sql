-- V2__seed_admin_account.sql
-- Khởi tạo tài khoản Quản trị viên (Admin) mặc định theo quy định AGENTS.md

INSERT INTO users (id, email, role, status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin@pilates.com',
    'ADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

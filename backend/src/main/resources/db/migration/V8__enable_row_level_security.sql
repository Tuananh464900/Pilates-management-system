-- V8__enable_row_level_security.sql
-- Kich hoat Row-Level Security (RLS) cho toan bo bang trong schema public
-- Ngan chan truy cap trai phep qua Supabase PostgREST / Data API (Anon Key)
-- Bao ve du lieu nhay cam nguoi dung (users, profiles, bookings, payments, sessions)

ALTER TABLE IF EXISTS users ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS member_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS trainer_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS pilates_classes ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS membership_packages ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS member_memberships ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS system_audit_logs ENABLE ROW LEVEL SECURITY;

-- Ghi nhan hanh dong bao mat vao nhat ky kiem toan
INSERT INTO system_audit_logs (action, entity_name, entity_id, details)
VALUES (
    'ENABLE_ROW_LEVEL_SECURITY',
    'SECURITY_POLICY',
    'V8',
    'Da kich hoat Row-Level Security tren 11 bang public. Khoa toan bo truy cap trai phep qua PostgREST API.'
);


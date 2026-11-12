-- V3__init_classes_rooms_sessions.sql
-- Khởi tạo bảng cho Lớp Pilates mẫu, Phòng tập luyện và Buổi tập theo lịch (UC2)

-- 1. Bảng pilates_classes: Lớp học mẫu / Môn học
CREATE TABLE pilates_classes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    default_capacity INT NOT NULL CHECK (default_capacity > 0),
    level VARCHAR(30) NOT NULL DEFAULT 'BEGINNER' CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'ALL_LEVELS')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_classes_status ON pilates_classes(status);

-- 2. Bảng rooms: Phòng tập luyện
CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    capacity INT NOT NULL CHECK (capacity > 0),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rooms_status ON rooms(status);

-- 3. Bảng sessions: Buổi tập cụ thể theo lịch
CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES pilates_classes(id),
    trainer_id UUID NOT NULL REFERENCES users(id),
    room_id BIGINT NOT NULL REFERENCES rooms(id),
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    booked_count INT NOT NULL DEFAULT 0 CHECK (booked_count >= 0 AND booked_count <= capacity),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELED')),
    cancel_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_session_time CHECK (end_time > start_time)
);

CREATE INDEX idx_sessions_time ON sessions(start_time, end_time);
CREATE INDEX idx_sessions_trainer_time ON sessions(trainer_id, start_time, end_time);
CREATE INDEX idx_sessions_room_time ON sessions(room_id, start_time, end_time);
CREATE INDEX idx_sessions_status ON sessions(status);

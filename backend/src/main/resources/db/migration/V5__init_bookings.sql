-- V5: Schema cho Đặt chỗ và Hủy buổi tập (UC4.1 - UC4.3)

CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    member_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    session_id BIGINT NOT NULL REFERENCES sessions(id) ON DELETE RESTRICT,
    membership_id BIGINT NOT NULL REFERENCES member_memberships(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED' CHECK (status IN ('CONFIRMED', 'CANCELED', 'ATTENDED', 'ABSENT')),
    refunded BOOLEAN NOT NULL DEFAULT FALSE,
    cancel_reason VARCHAR(255),
    canceled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Mỗi hội viên chỉ được có tối đa 1 đăng ký CONFIRMED cho cùng một buổi tập
CREATE UNIQUE INDEX IF NOT EXISTS uq_booking_member_session_confirmed 
ON bookings(member_id, session_id) 
WHERE status = 'CONFIRMED';

CREATE INDEX IF NOT EXISTS idx_bookings_member_id ON bookings(member_id);
CREATE INDEX IF NOT EXISTS idx_bookings_session_id ON bookings(session_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);

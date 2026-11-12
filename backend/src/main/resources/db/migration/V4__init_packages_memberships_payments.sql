-- V4: Schema cho Gói tập, Đăng ký hội viên và Thanh toán (UC3.1 - UC3.5)

-- 1. Bảng danh mục gói tập (membership_packages)
CREATE TABLE IF NOT EXISTS membership_packages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    total_sessions INT NOT NULL CHECK (total_sessions > 0),
    duration_days INT NOT NULL CHECK (duration_days > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng gói tập của hội viên (member_memberships)
CREATE TABLE IF NOT EXISTS member_memberships (
    id BIGSERIAL PRIMARY KEY,
    member_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    package_id BIGINT NOT NULL REFERENCES membership_packages(id) ON DELETE RESTRICT,
    remaining_sessions INT NOT NULL CHECK (remaining_sessions >= 0),
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'EXHAUSTED', 'CANCELED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_dates CHECK (end_date >= start_date)
);

CREATE INDEX IF NOT EXISTS idx_memberships_member_status ON member_memberships(member_id, status);

-- 3. Bảng giao dịch thanh toán (payments)
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    transaction_code VARCHAR(50) NOT NULL UNIQUE,
    member_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    package_id BIGINT NOT NULL REFERENCES membership_packages(id) ON DELETE RESTRICT,
    membership_id BIGINT REFERENCES member_memberships(id) ON DELETE SET NULL,
    amount NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    payment_method VARCHAR(30) NOT NULL DEFAULT 'BANK_TRANSFER' CHECK (payment_method IN ('CASH', 'BANK_TRANSFER')),
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID' CHECK (status IN ('UNPAID', 'PAID', 'CANCELED')),
    paid_at TIMESTAMP WITH TIME ZONE,
    note TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_payments_member_id ON payments(member_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);

-- 4. Dữ liệu mẫu ban đầu cho một số gói tập Pilates phổ biến
INSERT INTO membership_packages (name, description, price, total_sessions, duration_days, status)
VALUES 
    ('Gói Khám Phá (10 buổi)', 'Gói tập cơ bản dành cho người mới bắt đầu làm quen với Pilates Mat & Reformer', 1500000, 10, 30, 'ACTIVE'),
    ('Gói Tiêu Chuẩn (20 buổi)', 'Gói tập phù hợp duy trì vóc dáng, rèn luyện độ dẻo dai và cơ lõi', 2800000, 20, 60, 'ACTIVE'),
    ('Gói Chuyên Sâu (36 buổi)', 'Gói tập toàn diện kết hợp chuyên sâu Reformer, Cadillac và Chair cùng HLV', 4800000, 36, 90, 'ACTIVE'),
    ('Gói Cao Cấp VIP (60 buổi)', 'Gói tập linh hoạt dài hạn, ưu tiên đặt lịch tập mọi khung giờ trong ngày', 7500000, 60, 180, 'ACTIVE')
ON CONFLICT DO NOTHING;

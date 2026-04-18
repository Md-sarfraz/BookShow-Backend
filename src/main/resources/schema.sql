CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    is_read BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (id)
);

-- Ensure bookings payment status supports EXPIRED state introduced in backend enum.
-- This statement is safe with spring.sql.init.continue-on-error=true when table is absent.
ALTER TABLE bookings
    MODIFY COLUMN payment_status ENUM('PENDING','CONFIRMED','FAILED','EXPIRED','CANCELLED')
    NOT NULL DEFAULT 'PENDING';


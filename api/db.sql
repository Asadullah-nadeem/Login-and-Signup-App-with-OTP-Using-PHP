CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    otp VARCHAR(6),
    otp_expiry DATETIME,
    is_verified BOOLEAN DEFAULT 0,
    login_attempts INT DEFAULT 0,
    account_locked_until DATETIME,
    reset_token VARCHAR(64),
    reset_token_expiry DATETIME
);

CREATE INDEX idx_email ON users(email);
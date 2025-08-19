-- 사용자 테이블 생성
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    reg_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_username ON user(username);
CREATE INDEX idx_email ON user(email);

-- 테스트 데이터 (선택사항)
-- INSERT INTO user (username, password, name, email) VALUES 
-- ('admin', '21232f297a57a5a743894a0e4a801fc3', '관리자', 'admin@example.com'),
-- ('test', '098f6bcd4621d373cade4e832627b4f6', '테스트', 'test@example.com');

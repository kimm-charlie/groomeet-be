CREATE TABLE forbidden_word (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    word VARCHAR(100) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 초기 금칙어 삽입
INSERT INTO forbidden_word (word) VALUES ('반영구'), ('문신'), ('타투'), ('시술');

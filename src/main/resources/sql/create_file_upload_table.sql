-- 파일 업로드 테이블 생성
CREATE TABLE IF NOT EXISTS file_upload (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_type VARCHAR(20) NOT NULL,  -- 'file' 또는 'image'
    web_url VARCHAR(500),            -- 웹에서 접근 가능한 URL (이미지인 경우)
    
    -- 외래키 제약조건
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE,
    
    -- 인덱스
    INDEX idx_board_id (board_id),
    INDEX idx_stored_filename (stored_filename),
    INDEX idx_file_type (file_type),
    INDEX idx_upload_date (upload_date)
);

-- 테이블 코멘트
COMMENT ON TABLE file_upload IS '파일 업로드 정보 테이블';
COMMENT ON COLUMN file_upload.id IS '파일 ID (자동 증가)';
COMMENT ON COLUMN file_upload.board_id IS '게시글 ID (외래키)';
COMMENT ON COLUMN file_upload.original_filename IS '원본 파일명';
COMMENT ON COLUMN file_upload.stored_filename IS '저장된 파일명 (UUID 기반)';
COMMENT ON COLUMN file_upload.file_path IS '물리적 파일 경로';
COMMENT ON COLUMN file_upload.file_size IS '파일 크기 (바이트)';
COMMENT ON COLUMN file_upload.content_type IS '파일 MIME 타입';
COMMENT ON COLUMN file_upload.upload_date IS '업로드 날짜';
COMMENT ON COLUMN file_upload.file_type IS '파일 타입 (file/image)';
COMMENT ON COLUMN file_upload.web_url IS '웹 접근 URL (이미지인 경우)';

-- OCR Record table for bill import
CREATE TABLE ocr_record (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    original_filename VARCHAR(255),
    ocr_raw_text TEXT,
    extracted_amount DECIMAL(12, 2),
    extracted_merchant VARCHAR(200),
    extracted_date DATE,
    suggested_category_id BIGINT,
    transaction_id BIGINT,
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (suggested_category_id) REFERENCES category(id),
    FOREIGN KEY (transaction_id) REFERENCES transaction(id)
);

CREATE INDEX idx_ocr_record_user_id ON ocr_record(user_id);
CREATE INDEX idx_ocr_record_status ON ocr_record(status);
CREATE INDEX idx_ocr_record_user_status ON ocr_record(user_id, status);

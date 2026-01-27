-- Transaction table
CREATE TABLE transaction (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    type SMALLINT NOT NULL,
    category_id BIGINT NOT NULL,
    description VARCHAR(255),
    transaction_date DATE NOT NULL,
    payment_method VARCHAR(50),
    merchant VARCHAR(100),
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE INDEX idx_transaction_user_id ON transaction(user_id);
CREATE INDEX idx_transaction_date ON transaction(transaction_date);
CREATE INDEX idx_transaction_category ON transaction(category_id);
CREATE INDEX idx_transaction_type ON transaction(type);
CREATE INDEX idx_transaction_user_date ON transaction(user_id, transaction_date);
CREATE INDEX idx_transaction_user_type_date ON transaction(user_id, type, transaction_date);

COMMENT ON TABLE transaction IS 'Transaction record table';
COMMENT ON COLUMN transaction.type IS 'Transaction type: 1=income, 2=expense';
COMMENT ON COLUMN transaction.amount IS 'Amount in CNY';
COMMENT ON COLUMN transaction.is_deleted IS 'Soft delete flag';

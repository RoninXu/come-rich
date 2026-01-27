-- Category table
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT,
    type SMALLINT NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20),
    sort_order INT DEFAULT 0,
    is_system BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (parent_id) REFERENCES category(id) ON DELETE SET NULL
);

CREATE INDEX idx_category_type ON category(type);
CREATE INDEX idx_category_parent ON category(parent_id);

COMMENT ON TABLE category IS 'Transaction category table';
COMMENT ON COLUMN category.type IS 'Type: 1=income, 2=expense';
COMMENT ON COLUMN category.is_system IS 'System categories cannot be deleted';

-- Budget management tables

CREATE TABLE budget (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    year_month VARCHAR(7) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    note VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id),
    CONSTRAINT uk_budget_user_category_month UNIQUE (user_id, category_id, year_month)
);

CREATE INDEX idx_budget_user_id ON budget(user_id);
CREATE INDEX idx_budget_user_month ON budget(user_id, year_month);

CREATE TABLE budget_total (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    year_month VARCHAR(7) NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT uk_budget_total_user_month UNIQUE (user_id, year_month)
);

CREATE INDEX idx_budget_total_user_month ON budget_total(user_id, year_month);

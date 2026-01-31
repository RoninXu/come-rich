-- Financial Goal table
CREATE TABLE financial_goal (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    target_amount DECIMAL(12, 2) NOT NULL,
    current_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    deadline DATE NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    priority SMALLINT NOT NULL DEFAULT 2,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_goal_user_id ON financial_goal(user_id);
CREATE INDEX idx_goal_status ON financial_goal(status);
CREATE INDEX idx_goal_user_status ON financial_goal(user_id, status);

-- Goal Progress table
CREATE TABLE goal_progress (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    note VARCHAR(255),
    record_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goal_id) REFERENCES financial_goal(id) ON DELETE CASCADE
);

CREATE INDEX idx_goal_progress_goal_id ON goal_progress(goal_id);
CREATE INDEX idx_goal_progress_record_date ON goal_progress(record_date);

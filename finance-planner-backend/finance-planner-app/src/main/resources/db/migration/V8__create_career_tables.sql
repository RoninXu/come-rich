-- User Profile table
CREATE TABLE user_profile (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    occupation VARCHAR(100),
    skills TEXT,
    available_hours_per_week INTEGER,
    income_expectation DECIMAL(12, 2),
    interests TEXT,
    experience_level VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_profile_user_id ON user_profile(user_id);

-- Career Plan table
CREATE TABLE career_plan (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    career_type VARCHAR(100),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    match_score INTEGER,
    status SMALLINT NOT NULL DEFAULT 1,
    target_monthly_income DECIMAL(12, 2),
    actual_monthly_income DECIMAL(12, 2) DEFAULT 0,
    start_date DATE,
    end_date DATE,
    startup_plan TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_career_plan_user_id ON career_plan(user_id);
CREATE INDEX idx_career_plan_status ON career_plan(status);
CREATE INDEX idx_career_plan_user_status ON career_plan(user_id, status);

-- Career Income table
CREATE TABLE career_income (
    id BIGSERIAL PRIMARY KEY,
    career_plan_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    description VARCHAR(255),
    income_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (career_plan_id) REFERENCES career_plan(id) ON DELETE CASCADE
);

CREATE INDEX idx_career_income_plan_id ON career_income(career_plan_id);
CREATE INDEX idx_career_income_date ON career_income(income_date);

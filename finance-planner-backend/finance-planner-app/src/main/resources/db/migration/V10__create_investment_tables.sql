-- Investment advice and risk assessment tables

CREATE TABLE risk_assessment (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    answers JSONB NOT NULL,
    risk_score INTEGER NOT NULL,
    risk_level VARCHAR(30) NOT NULL,
    assessment_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_risk_assessment_user_id ON risk_assessment(user_id);

CREATE TABLE investment_recommendation (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    risk_assessment_id BIGINT NOT NULL,
    track_name VARCHAR(100) NOT NULL,
    allocation_percentage DECIMAL(5, 2) NOT NULL,
    description TEXT,
    rationale TEXT,
    risk_level VARCHAR(30),
    expected_annual_return VARCHAR(50),
    status SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (risk_assessment_id) REFERENCES risk_assessment(id) ON DELETE CASCADE
);

CREATE INDEX idx_investment_rec_user_id ON investment_recommendation(user_id);
CREATE INDEX idx_investment_rec_status ON investment_recommendation(user_id, status);

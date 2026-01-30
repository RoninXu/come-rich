-- AI Conversation table for storing chat history
CREATE TABLE ai_conversation (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(64) NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    tokens INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_ai_conversation_user_id ON ai_conversation(user_id);
CREATE INDEX idx_ai_conversation_session_id ON ai_conversation(session_id);
CREATE INDEX idx_ai_conversation_user_session ON ai_conversation(user_id, session_id);
CREATE INDEX idx_ai_conversation_created_at ON ai_conversation(created_at);

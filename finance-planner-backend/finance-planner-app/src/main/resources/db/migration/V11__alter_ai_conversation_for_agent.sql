-- Add agent fields to ai_conversation
ALTER TABLE ai_conversation
    ADD COLUMN message_type VARCHAR(20),
    ADD COLUMN tool_calls TEXT,
    ADD COLUMN tool_call_id VARCHAR(80);

CREATE INDEX idx_ai_conversation_message_type ON ai_conversation(message_type);
CREATE INDEX idx_ai_conversation_tool_call_id ON ai_conversation(tool_call_id);

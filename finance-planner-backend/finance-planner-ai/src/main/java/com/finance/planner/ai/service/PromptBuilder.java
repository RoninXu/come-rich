package com.finance.planner.ai.service;

import java.util.List;
import java.util.Map;

public interface PromptBuilder {

    String buildSystemPrompt();

    List<Map<String, String>> buildMessages(Long userId, String sessionId, String userMessage);
}

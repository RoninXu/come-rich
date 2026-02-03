package com.finance.planner.ai.service;

import com.finance.planner.ai.dto.LlmStreamEvent;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface LlmClient {

    Flux<String> streamChat(List<Map<String, String>> messages);

    Flux<LlmStreamEvent> streamChatWithTools(
            List<Map<String, Object>> messages,
            List<Map<String, Object>> tools,
            String toolChoice
    );
}

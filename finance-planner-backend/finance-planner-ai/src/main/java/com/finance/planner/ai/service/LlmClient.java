package com.finance.planner.ai.service;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface LlmClient {

    Flux<String> streamChat(List<Map<String, String>> messages);
}

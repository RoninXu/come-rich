package com.finance.planner.ai.service;

public interface RateLimitService {

    boolean isAllowed(Long userId);

    int getRemainingChats(Long userId);
}

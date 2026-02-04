package com.finance.planner.ai.time;

public interface TimeContextProvider {

    TimeContext getTimeContext(Long userId, String sessionId);
}

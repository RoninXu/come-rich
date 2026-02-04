package com.finance.planner.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionStatusDto {

    private String sessionId;
    private Long userId;
    private String serverTime;
    private String serverDate;
    private String timezone;
    private String clockSource;
    private String activeProvider;
    private String model;
}

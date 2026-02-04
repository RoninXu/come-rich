package com.finance.planner.ai.time;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeContext {

    private String serverTime;
    private String serverDate;
    private String timezone;
    private String clockSource;
}

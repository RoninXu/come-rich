package com.finance.planner.ai.agent.tool.params;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ListTransactionsParams {

    private Short type;
    private String startDate;
    private String endDate;

    @Min(1)
    private Integer page = 1;

    @Min(1)
    @Max(100)
    private Integer pageSize = 20;
}

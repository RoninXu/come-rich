package com.finance.planner.ai.agent.tool.params;

import lombok.Data;

@Data
public class GetCategoriesParams {

    private Short type;
    private Boolean tree = Boolean.FALSE;
}

package com.finance.planner.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallChunk {

    private Integer index;
    private String id;
    private String name;
    private String arguments;
}

package com.finance.planner.ai.agent.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final List<Tool> tools;
    private final ToolSchemaGenerator schemaGenerator;

    public List<Tool> getTools() {
        return tools.stream()
                .sorted(Comparator.comparing(Tool::getName))
                .toList();
    }

    public Tool getTool(String name) {
        return tools.stream()
                .filter(tool -> tool.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<Map<String, Object>> getToolSchemas() {
        return getTools().stream()
                .map(schemaGenerator::buildToolSchema)
                .collect(Collectors.toList());
    }
}

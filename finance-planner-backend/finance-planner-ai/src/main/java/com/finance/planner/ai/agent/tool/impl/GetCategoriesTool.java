package com.finance.planner.ai.agent.tool.impl;

import com.finance.planner.accounting.service.CategoryService;
import com.finance.planner.ai.agent.tool.AbstractTool;
import com.finance.planner.ai.agent.tool.AgentTool;
import com.finance.planner.ai.agent.tool.RiskLevel;
import com.finance.planner.ai.agent.tool.ToolResult;
import com.finance.planner.ai.agent.tool.params.GetCategoriesParams;
import org.springframework.stereotype.Component;

@Component
@AgentTool(name = "get_categories", description = "获取分类列表或分类树", riskLevel = RiskLevel.LOW)
public class GetCategoriesTool extends AbstractTool<GetCategoriesParams> {

    private final CategoryService categoryService;

    public GetCategoriesTool(CategoryService categoryService) {
        super(GetCategoriesParams.class);
        this.categoryService = categoryService;
    }

    @Override
    protected ToolResult executeInternal(Long userId, GetCategoriesParams params) {
        boolean tree = Boolean.TRUE.equals(params.getTree());
        if (params.getType() != null) {
            return ToolResult.success(tree
                    ? categoryService.getCategoryTreeByType(params.getType())
                    : categoryService.getCategoriesByType(params.getType()));
        }
        return ToolResult.success(tree ? categoryService.getCategoryTree() : categoryService.getAllCategories());
    }
}

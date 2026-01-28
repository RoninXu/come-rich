package com.finance.planner.accounting.controller;

import com.finance.planner.accounting.dto.CategoryDto;
import com.finance.planner.accounting.service.CategoryService;
import com.finance.planner.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories", description = "Get all categories as a flat list, optionally filtered by type")
    public ApiResponse<List<CategoryDto>> getCategories(
            @Parameter(description = "Category type: 1=income, 2=expense")
            @RequestParam(required = false) Short type) {
        List<CategoryDto> categories;
        if (type != null) {
            categories = categoryService.getCategoriesByType(type);
        } else {
            categories = categoryService.getAllCategories();
        }
        return ApiResponse.success(categories);
    }

    @GetMapping("/tree")
    @Operation(summary = "Get category tree", description = "Get categories in a hierarchical tree structure")
    public ApiResponse<List<CategoryDto>> getCategoryTree(
            @Parameter(description = "Category type: 1=income, 2=expense")
            @RequestParam(required = false) Short type) {
        List<CategoryDto> tree;
        if (type != null) {
            tree = categoryService.getCategoryTreeByType(type);
        } else {
            tree = categoryService.getCategoryTree();
        }
        return ApiResponse.success(tree);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ApiResponse<CategoryDto> getCategory(
            @Parameter(description = "Category ID")
            @PathVariable Long id) {
        CategoryDto category = categoryService.getCategory(id);
        return ApiResponse.success(category);
    }
}

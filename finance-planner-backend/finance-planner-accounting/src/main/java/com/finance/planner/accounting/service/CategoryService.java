package com.finance.planner.accounting.service;

import com.finance.planner.accounting.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    /**
     * Get all categories as a flat list
     */
    List<CategoryDto> getAllCategories();

    /**
     * Get categories by type (1=income, 2=expense)
     */
    List<CategoryDto> getCategoriesByType(Short type);

    /**
     * Get categories as a tree structure (parent with children)
     */
    List<CategoryDto> getCategoryTree();

    /**
     * Get category tree by type
     */
    List<CategoryDto> getCategoryTreeByType(Short type);

    /**
     * Get a single category by ID
     */
    CategoryDto getCategory(Long id);
}

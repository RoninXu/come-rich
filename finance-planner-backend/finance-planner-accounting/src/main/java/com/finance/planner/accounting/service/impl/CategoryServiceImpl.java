package com.finance.planner.accounting.service.impl;

import com.finance.planner.accounting.dto.CategoryDto;
import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.accounting.service.CategoryService;
import com.finance.planner.common.constant.ErrorCode;
import com.finance.planner.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAllByOrderByTypeAscSortOrderAsc();
        return categories.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getCategoriesByType(Short type) {
        List<Category> categories = categoryRepository.findAllByTypeOrderBySortOrderAsc(type);
        return categories.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getCategoryTree() {
        // Get all parent categories
        List<Category> parentCategories = categoryRepository.findAllByParentIdIsNullOrderBySortOrderAsc();

        return parentCategories.stream()
                .map(this::buildCategoryWithChildren)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getCategoryTreeByType(Short type) {
        // Get parent categories by type
        List<Category> parentCategories = categoryRepository.findAllByParentIdIsNullAndTypeOrderBySortOrderAsc(type);

        return parentCategories.stream()
                .map(this::buildCategoryWithChildren)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        return CategoryDto.fromEntity(category);
    }

    /**
     * Build category DTO with its children
     */
    private CategoryDto buildCategoryWithChildren(Category parent) {
        List<Category> children = categoryRepository.findAllByParentIdOrderBySortOrderAsc(parent.getId());
        List<CategoryDto> childrenDtos = children.stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());

        return CategoryDto.fromEntityWithChildren(parent, childrenDtos);
    }
}

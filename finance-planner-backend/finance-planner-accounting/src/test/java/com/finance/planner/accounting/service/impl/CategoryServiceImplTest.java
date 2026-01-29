package com.finance.planner.accounting.service.impl;

import com.finance.planner.accounting.dto.CategoryDto;
import com.finance.planner.accounting.entity.Category;
import com.finance.planner.accounting.repository.CategoryRepository;
import com.finance.planner.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Unit Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    // ========== Helper methods ==========

    private Category createCategory(Long id, String name, Short type, Long parentId) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setType(type);
        category.setParentId(parentId);
        category.setIcon("icon-" + name.toLowerCase());
        category.setColor("#FF0000");
        category.setSortOrder(1);
        category.setIsSystem(true);
        return category;
    }

    // ========== getAllCategories ==========

    @Test
    @DisplayName("getAllCategories - returns all categories as flat list")
    void getAllCategories_returnsFlatList() {
        // Given
        Category income = createCategory(1L, "Salary", (short) 1, null);
        Category expense = createCategory(2L, "Food", (short) 2, null);
        when(categoryRepository.findAllByOrderByTypeAscSortOrderAsc())
                .thenReturn(List.of(income, expense));

        // When
        List<CategoryDto> result = categoryService.getAllCategories();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Salary");
        assertThat(result.get(0).getType()).isEqualTo((short) 1);
        assertThat(result.get(1).getName()).isEqualTo("Food");
        assertThat(result.get(1).getType()).isEqualTo((short) 2);
        verify(categoryRepository).findAllByOrderByTypeAscSortOrderAsc();
    }

    // ========== getCategoriesByType ==========

    @Test
    @DisplayName("getCategoriesByType - returns categories filtered by type")
    void getCategoriesByType_returnsFilteredList() {
        // Given
        Short incomeType = 1;
        Category salary = createCategory(1L, "Salary", incomeType, null);
        Category bonus = createCategory(2L, "Bonus", incomeType, null);
        when(categoryRepository.findAllByTypeOrderBySortOrderAsc(incomeType))
                .thenReturn(List.of(salary, bonus));

        // When
        List<CategoryDto> result = categoryService.getCategoriesByType(incomeType);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(dto -> dto.getType().equals(incomeType));
        verify(categoryRepository).findAllByTypeOrderBySortOrderAsc(incomeType);
    }

    @Test
    @DisplayName("getCategoriesByType - returns income type categories")
    void getCategoriesByType_incomeType() {
        // Given
        Short incomeType = 1;
        Category salary = createCategory(1L, "Salary", incomeType, null);
        when(categoryRepository.findAllByTypeOrderBySortOrderAsc(incomeType))
                .thenReturn(List.of(salary));

        // When
        List<CategoryDto> result = categoryService.getCategoriesByType(incomeType);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Salary");
        assertThat(result.get(0).getType()).isEqualTo(incomeType);
    }

    // ========== getCategoryTree ==========

    @Test
    @DisplayName("getCategoryTree - builds tree with parent-child hierarchy")
    void getCategoryTree_buildsCorrectly() {
        // Given
        Category parent = createCategory(1L, "Food", (short) 2, null);
        Category child1 = createCategory(2L, "Groceries", (short) 2, 1L);
        Category child2 = createCategory(3L, "Restaurant", (short) 2, 1L);

        when(categoryRepository.findAllByParentIdIsNullOrderBySortOrderAsc())
                .thenReturn(List.of(parent));
        when(categoryRepository.findAllByParentIdOrderBySortOrderAsc(1L))
                .thenReturn(List.of(child1, child2));

        // When
        List<CategoryDto> result = categoryService.getCategoryTree();

        // Then
        assertThat(result).hasSize(1);
        CategoryDto parentDto = result.get(0);
        assertThat(parentDto.getName()).isEqualTo("Food");
        assertThat(parentDto.getChildren()).hasSize(2);
        assertThat(parentDto.getChildren().get(0).getName()).isEqualTo("Groceries");
        assertThat(parentDto.getChildren().get(1).getName()).isEqualTo("Restaurant");
        verify(categoryRepository).findAllByParentIdIsNullOrderBySortOrderAsc();
        verify(categoryRepository).findAllByParentIdOrderBySortOrderAsc(1L);
    }

    @Test
    @DisplayName("getCategoryTree - returns empty list when no categories exist")
    void getCategoryTree_emptyResult() {
        // Given
        when(categoryRepository.findAllByParentIdIsNullOrderBySortOrderAsc())
                .thenReturn(Collections.emptyList());

        // When
        List<CategoryDto> result = categoryService.getCategoryTree();

        // Then
        assertThat(result).isEmpty();
        verify(categoryRepository).findAllByParentIdIsNullOrderBySortOrderAsc();
        verify(categoryRepository, never()).findAllByParentIdOrderBySortOrderAsc(anyLong());
    }

    // ========== getCategoryTreeByType ==========

    @Test
    @DisplayName("getCategoryTreeByType - builds tree filtered by type")
    void getCategoryTreeByType_buildsFilteredTree() {
        // Given
        Short expenseType = 2;
        Category parent = createCategory(1L, "Food", expenseType, null);
        Category child = createCategory(2L, "Groceries", expenseType, 1L);

        when(categoryRepository.findAllByParentIdIsNullAndTypeOrderBySortOrderAsc(expenseType))
                .thenReturn(List.of(parent));
        when(categoryRepository.findAllByParentIdOrderBySortOrderAsc(1L))
                .thenReturn(List.of(child));

        // When
        List<CategoryDto> result = categoryService.getCategoryTreeByType(expenseType);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getName()).isEqualTo("Groceries");
        verify(categoryRepository).findAllByParentIdIsNullAndTypeOrderBySortOrderAsc(expenseType);
    }

    // ========== getCategory ==========

    @Test
    @DisplayName("getCategory - returns category when found")
    void getCategory_found() {
        // Given
        Long categoryId = 1L;
        Category category = createCategory(categoryId, "Salary", (short) 1, null);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // When
        CategoryDto result = categoryService.getCategory(categoryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo("Salary");
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("getCategory - throws BusinessException when not found")
    void getCategory_notFound_throwsException() {
        // Given
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> categoryService.getCategory(categoryId))
                .isInstanceOf(BusinessException.class);
        verify(categoryRepository).findById(categoryId);
    }
}

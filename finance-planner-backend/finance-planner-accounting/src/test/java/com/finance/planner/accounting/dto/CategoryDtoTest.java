package com.finance.planner.accounting.dto;

import com.finance.planner.accounting.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryDto Unit Tests")
class CategoryDtoTest {

    // ========== Helper methods ==========

    private Category createCategory(Long id, String name, Short type, Long parentId) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setType(type);
        category.setParentId(parentId);
        category.setIcon("icon-test");
        category.setColor("#00FF00");
        category.setSortOrder(5);
        category.setIsSystem(true);
        return category;
    }

    // ========== fromEntity ==========

    @Test
    @DisplayName("fromEntity - maps all fields correctly")
    void fromEntity_mapsAllFields() {
        // Given
        Category category = createCategory(1L, "Food", (short) 2, 10L);

        // When
        CategoryDto dto = CategoryDto.fromEntity(category);

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Food");
        assertThat(dto.getType()).isEqualTo((short) 2);
        assertThat(dto.getParentId()).isEqualTo(10L);
        assertThat(dto.getIcon()).isEqualTo("icon-test");
        assertThat(dto.getColor()).isEqualTo("#00FF00");
        assertThat(dto.getSortOrder()).isEqualTo(5);
        assertThat(dto.getIsSystem()).isTrue();
        assertThat(dto.getChildren()).isNull();
    }

    // ========== fromEntityWithChildren ==========

    @Test
    @DisplayName("fromEntityWithChildren - includes children list")
    void fromEntityWithChildren() {
        // Given
        Category parent = createCategory(1L, "Food", (short) 2, null);
        Category child1 = createCategory(2L, "Groceries", (short) 2, 1L);
        Category child2 = createCategory(3L, "Restaurant", (short) 2, 1L);

        List<CategoryDto> children = List.of(
                CategoryDto.fromEntity(child1),
                CategoryDto.fromEntity(child2)
        );

        // When
        CategoryDto dto = CategoryDto.fromEntityWithChildren(parent, children);

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Food");
        assertThat(dto.getChildren()).hasSize(2);
        assertThat(dto.getChildren().get(0).getName()).isEqualTo("Groceries");
        assertThat(dto.getChildren().get(1).getName()).isEqualTo("Restaurant");
    }

    // ========== fromEntity with null parentId ==========

    @Test
    @DisplayName("fromEntity - handles null parentId correctly")
    void fromEntity_nullParentId() {
        // Given
        Category category = createCategory(1L, "Income", (short) 1, null);

        // When
        CategoryDto dto = CategoryDto.fromEntity(category);

        // Then
        assertThat(dto.getParentId()).isNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Income");
    }
}

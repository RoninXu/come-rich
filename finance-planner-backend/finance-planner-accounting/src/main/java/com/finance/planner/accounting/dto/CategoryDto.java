package com.finance.planner.accounting.dto;

import com.finance.planner.accounting.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;
    private String name;
    private Long parentId;
    private Short type;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Boolean isSystem;

    /**
     * Subcategories (only populated in tree structure response)
     */
    private List<CategoryDto> children;

    /**
     * Convert entity to DTO (without children)
     */
    public static CategoryDto fromEntity(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId(category.getParentId())
                .type(category.getType())
                .icon(category.getIcon())
                .color(category.getColor())
                .sortOrder(category.getSortOrder())
                .isSystem(category.getIsSystem())
                .build();
    }

    /**
     * Convert entity to DTO with children
     */
    public static CategoryDto fromEntityWithChildren(Category category, List<CategoryDto> children) {
        CategoryDto dto = fromEntity(category);
        dto.setChildren(children);
        return dto;
    }
}

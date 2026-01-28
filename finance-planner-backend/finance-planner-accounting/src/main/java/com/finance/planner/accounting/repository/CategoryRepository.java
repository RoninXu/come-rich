package com.finance.planner.accounting.repository;

import com.finance.planner.accounting.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find all categories by type, ordered by sort_order
     */
    List<Category> findAllByTypeOrderBySortOrderAsc(Short type);

    /**
     * Find all parent categories (categories without parent_id)
     */
    List<Category> findAllByParentIdIsNullOrderBySortOrderAsc();

    /**
     * Find all parent categories by type
     */
    List<Category> findAllByParentIdIsNullAndTypeOrderBySortOrderAsc(Short type);

    /**
     * Find all subcategories of a parent category
     */
    List<Category> findAllByParentIdOrderBySortOrderAsc(Long parentId);

    /**
     * Find all categories ordered by type and sort_order
     */
    List<Category> findAllByOrderByTypeAscSortOrderAsc();
}

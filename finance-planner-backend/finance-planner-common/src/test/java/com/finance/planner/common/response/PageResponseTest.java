package com.finance.planner.common.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PageResponse Tests")
class PageResponseTest {

    @Test
    @DisplayName("Constructor should set all fields correctly including totalPages calculation")
    void testConstructor() {
        List<String> items = Arrays.asList("item1", "item2", "item3");

        PageResponse<String> response = new PageResponse<>(items, 25L, 1, 10);

        assertEquals(items, response.getList());
        assertEquals(25L, response.getTotal());
        assertEquals(1, response.getPage());
        assertEquals(10, response.getPageSize());
        assertEquals(3, response.getTotalPages()); // ceil(25/10) = 3
    }

    @Test
    @DisplayName("of(Page) should convert Spring Data Page correctly with 1-based page number")
    void testOfWithPage() {
        List<String> content = Arrays.asList("a", "b", "c");
        // Spring Data Page uses 0-based page numbers
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<String> springPage = new PageImpl<>(content, pageRequest, 30L);

        PageResponse<String> response = PageResponse.of(springPage);

        assertEquals(content, response.getList());
        assertEquals(30L, response.getTotal());
        assertEquals(1, response.getPage()); // 0-based converted to 1-based
        assertEquals(10, response.getPageSize());
        assertEquals(3, response.getTotalPages()); // ceil(30/10) = 3
    }

    @Test
    @DisplayName("of(list, total, page, pageSize) should create PageResponse from raw values")
    void testOfWithList() {
        List<Integer> items = Arrays.asList(1, 2, 3, 4, 5);

        PageResponse<Integer> response = PageResponse.of(items, 50L, 2, 5);

        assertEquals(items, response.getList());
        assertEquals(50L, response.getTotal());
        assertEquals(2, response.getPage());
        assertEquals(5, response.getPageSize());
        assertEquals(10, response.getTotalPages()); // ceil(50/5) = 10
    }

    @Test
    @DisplayName("TotalPages should be calculated correctly for various total/pageSize combinations")
    void testTotalPagesCalculation() {
        // Exact division: 20 items / 10 per page = 2 pages
        PageResponse<String> exactDivision = new PageResponse<>(Collections.emptyList(), 20L, 1, 10);
        assertEquals(2, exactDivision.getTotalPages());

        // Non-exact division: 21 items / 10 per page = 3 pages (ceiling)
        PageResponse<String> nonExactDivision = new PageResponse<>(Collections.emptyList(), 21L, 1, 10);
        assertEquals(3, nonExactDivision.getTotalPages());

        // Single item: 1 item / 10 per page = 1 page
        PageResponse<String> singleItem = new PageResponse<>(Collections.emptyList(), 1L, 1, 10);
        assertEquals(1, singleItem.getTotalPages());

        // Zero items: 0 items / 10 per page = 0 pages
        PageResponse<String> zeroItems = new PageResponse<>(Collections.emptyList(), 0L, 1, 10);
        assertEquals(0, zeroItems.getTotalPages());

        // Large dataset: 1000 items / 15 per page = 67 pages (ceil(1000/15) = 67)
        PageResponse<String> largeDataset = new PageResponse<>(Collections.emptyList(), 1000L, 1, 15);
        assertEquals(67, largeDataset.getTotalPages());
    }

    @Test
    @DisplayName("Zero pageSize should result in zero totalPages to avoid division by zero")
    void testZeroPageSize() {
        PageResponse<String> response = new PageResponse<>(Collections.emptyList(), 100L, 1, 0);

        assertEquals(0, response.getTotalPages());
        assertEquals(0, response.getPageSize());
        assertEquals(100L, response.getTotal());
    }
}

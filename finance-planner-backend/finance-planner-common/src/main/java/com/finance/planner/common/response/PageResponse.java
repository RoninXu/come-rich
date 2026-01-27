package com.finance.planner.common.response;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResponse<T> {

    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;

    public PageResponse() {
    }

    public PageResponse(List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1,
                page.getSize()
        );
    }

    public static <T> PageResponse<T> of(List<T> list, long total, int page, int pageSize) {
        return new PageResponse<>(list, total, page, pageSize);
    }
}

package com.ssp.comment.common;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private long total;
    private int page;
    private int pageSize;
    private List<T> list;

    public static <T> PageResult<T> of(long total, int page, int pageSize, List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setList(list);
        return result;
    }
}

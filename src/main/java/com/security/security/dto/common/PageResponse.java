package com.security.security.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int page;          // 0-based page number
    private int size;          // page size
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}

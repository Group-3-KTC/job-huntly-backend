package com.jobhuntly.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageEnvelope<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private String sort;

    public static <T> PageEnvelope<T> from(Page<T> p) {
        var e = new PageEnvelope<T>();
        e.content = p.getContent();
        e.pageNumber = p.getNumber();
        e.pageSize = p.getSize();
        e.totalElements = p.getTotalElements();
        e.totalPages = p.getTotalPages();
        e.first = p.isFirst();
        e.last = p.isLast();
        e.hasNext = p.hasNext();
        e.sort = (p.getSort() == null || p.getSort().isUnsorted()) ? "unsorted" : p.getSort().toString();
        return e;
    }
}

package org.oneog.uppickcommon.common.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GlobalPageResponse<T> {
    private final int page;
    private final int size;
    private final int totalPages;
    private final long totalElements;
    private final List<T> contents;

    public static <T> GlobalPageResponse<T> of(Page<T> pageData) {
        return GlobalPageResponse.<T>builder()
            .page(pageData.getNumber())
            .size(pageData.getSize())
            .totalPages(pageData.getTotalPages())
            .totalElements(pageData.getTotalElements())
            .contents(pageData.getContent())
            .build();
    }
}
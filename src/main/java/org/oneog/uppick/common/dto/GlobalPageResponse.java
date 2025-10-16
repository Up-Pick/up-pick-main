package org.oneog.uppick.common.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GlobalPageResponse<T> {
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
    private List<T> contents;

    public static <T> GlobalPageResponse<T> of(Page<T> pageData) {
        GlobalPageResponse<T> response = new GlobalPageResponse<>();
        response.page = pageData.getNumber();
        response.size = pageData.getSize();
        response.totalPages = pageData.getTotalPages();
        response.totalElements = pageData.getTotalElements();
        response.contents = pageData.getContent();
        return response;
    }
}

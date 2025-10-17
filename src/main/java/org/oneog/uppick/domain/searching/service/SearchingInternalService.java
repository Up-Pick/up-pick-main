package org.oneog.uppick.domain.searching.service;

import java.time.LocalDateTime;

import org.oneog.uppick.domain.searching.dto.projection.SearchProductProjection;
import org.oneog.uppick.domain.searching.dto.request.SearchProductRequest;
import org.oneog.uppick.domain.searching.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.domain.searching.mapper.SearchingMapper;
import org.oneog.uppick.domain.searching.repository.SearchingQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchingInternalService {
    private final SearchingQueryRepository searchingQueryRepository;
    private final SearchingMapper searchingMapper;

    private static final long DEFAULT_CATEGORY_ID = 1L;
    private static final boolean DEFAULT_ONLY_NOT_SOLD = false;
    private static final int DEFAULT_SIZE = 20;

    public Page<SearchProductInfoResponse> searchProduct(SearchProductRequest searchProductRequest) {
        Long categoryId = searchProductRequest.getCategoryId();

        if (categoryId == null) {
            categoryId = DEFAULT_CATEGORY_ID;
        }

        Boolean onlyNotSold = searchProductRequest.getOnlyNotSold();

        if (onlyNotSold == null) {
            onlyNotSold = DEFAULT_ONLY_NOT_SOLD;
        }

        Integer page = searchProductRequest.getPage();

        if (page == null || page < 0) {
            page = 0;
        }

        Integer size = searchProductRequest.getSize();

        if (size == null || size <= 0) {
            size = DEFAULT_SIZE;
        }

        Pageable pageable = PageRequest.of(page, size);

        LocalDateTime endAtFrom;

        if (searchProductRequest.getEndAtFrom() != null) {
            endAtFrom = searchProductRequest.getEndAtFrom().atStartOfDay();
        } else {
            endAtFrom = null;
        }

        Page<SearchProductProjection> productProjections = searchingQueryRepository.findProductsWithFilters(
            pageable,
            categoryId,
            endAtFrom,
            onlyNotSold,
            searchProductRequest.getSortBy(),
            searchProductRequest.getKeyword());

        return searchingMapper.toResponse(productProjections);
    }
}

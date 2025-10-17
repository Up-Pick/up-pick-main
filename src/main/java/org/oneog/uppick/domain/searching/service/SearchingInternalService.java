package org.oneog.uppick.domain.searching.service;

import java.time.LocalDateTime;

import org.oneog.uppick.domain.searching.dto.projection.SearchProductProjection;
import org.oneog.uppick.domain.searching.dto.request.SearchProductRequest;
import org.oneog.uppick.domain.searching.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.domain.searching.entity.SearchHistory;
import org.oneog.uppick.domain.searching.mapper.SearchingMapper;
import org.oneog.uppick.domain.searching.repository.SearchHistoryJpaRepository;
import org.oneog.uppick.domain.searching.repository.SearchingQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchingInternalService {
    private final SearchingQueryRepository searchingQueryRepository;
    private final SearchingMapper searchingMapper;
    private final SearchHistoryJpaRepository searchHistoryJpaRepository;

    private static final long DEFAULT_CATEGORY_ID = 1L;
    private static final boolean DEFAULT_ONLY_NOT_SOLD = false;
    private static final int DEFAULT_SIZE = 20;

    @Transactional
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

        if (StringUtils.hasText(searchProductRequest.getKeyword())) {
            String[] keywords = searchProductRequest.getKeyword().trim().split(" ");
            for (String keyword : keywords) {
                searchHistoryJpaRepository.save(new SearchHistory(keyword));
            }
        }

        return searchingMapper.toResponse(productProjections);
    }
}

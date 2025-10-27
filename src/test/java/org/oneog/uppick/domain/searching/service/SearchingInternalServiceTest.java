package org.oneog.uppick.domain.searching.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.domain.searching.dto.projection.SearchProductProjection;
import org.oneog.uppick.domain.searching.dto.request.SearchProductRequest;
import org.oneog.uppick.domain.searching.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.domain.searching.entity.SearchHistory;
import org.oneog.uppick.domain.searching.mapper.SearchingMapper;
import org.oneog.uppick.domain.searching.repository.SearchHistoryJpaRepository;
import org.oneog.uppick.domain.searching.repository.SearchingQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class SearchingInternalServiceTest {

    @Mock
    SearchingQueryRepository searchingQueryRepository;

    @Mock
    SearchingMapper searchingMapper;

    @Mock
    SearchHistoryJpaRepository searchHistoryJpaRepository;

    @InjectMocks
    SearchingInternalService searchingInternalService;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2025, 1, 1, 12, 0);

    @BeforeEach
    void setUp() {
        given(searchHistoryJpaRepository.save(any(SearchHistory.class)))
            .willAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void searchProduct_입력값이_널이면_디폴트값을_사용한다() {
        LocalDate endAtFromDate = FIXED_NOW.toLocalDate();

        SearchProductRequest request = SearchProductRequest.builder()
            .keyword("키워드")
            .sortBy("endAtDesc")
            .endAtFrom(endAtFromDate)
            .build();

        SearchProductProjection p = new SearchProductProjection(
            1L,
            "img.jpg",
            "상품",
            FIXED_NOW.minusDays(1),
            FIXED_NOW.plusDays(1),
            5000L,
            1000L,
            false);

        Page<SearchProductProjection> projectionPage = new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1);

        given(searchingQueryRepository.findProductsWithFilters(any(Pageable.class), anyLong(), any(), anyBoolean(),
            anyString(), anyString()))
            .willReturn(projectionPage);

        SearchProductInfoResponse resp = SearchProductInfoResponse.builder()
            .id(1L)
            .image("img.jpg")
            .name("상품")
            .registeredAt(FIXED_NOW.minusDays(1))
            .endAt(FIXED_NOW.plusDays(1))
            .currentBidPrice(5000L)
            .minBidPrice(1000L)
            .isSold(false)
            .build();

        Page<SearchProductInfoResponse> responsePage = new PageImpl<>(List.of(resp), PageRequest.of(0, 20), 1);

        given(searchingMapper.toResponse(any())).willReturn(responsePage);

        Page<SearchProductInfoResponse> result = searchingInternalService.searchProduct(request);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        ArgumentCaptor<LocalDateTime> endAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(searchingQueryRepository).findProductsWithFilters(
            pageableCaptor.capture(),
            eq(1L),
            endAtCaptor.capture(),
            eq(false),
            eq("endAtDesc"),
            eq("키워드"));

        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
        assertThat(capturedPageable.getPageSize()).isEqualTo(20);

        LocalDateTime expectedStartOfDay = endAtFromDate.atStartOfDay();
        assertThat(endAtCaptor.getValue()).isEqualTo(expectedStartOfDay);
    }

    @Test
    void searchProduct_모든파라미터가_주어지면_그대로_사용한다() {
        SearchProductRequest request = SearchProductRequest.builder()
            .categoryId(2L)
            .onlyNotSold(true)
            .page(0)
            .size(5)
            .sortBy("someSort")
            .keyword("abc")
            .build();

        SearchProductProjection p = new SearchProductProjection(
            2L,
            "img2.jpg",
            "상품2",
            FIXED_NOW.minusDays(2),
            FIXED_NOW.plusDays(2),
            7000L,
            1500L,
            true);

        Page<SearchProductProjection> projectionPage = new PageImpl<>(List.of(p), PageRequest.of(0, 5), 1);

        given(searchingQueryRepository.findProductsWithFilters(any(Pageable.class), anyLong(), any(), anyBoolean(),
            anyString(), anyString()))
            .willReturn(projectionPage);

        SearchProductInfoResponse resp = SearchProductInfoResponse.builder()
            .id(2L)
            .image("img2.jpg")
            .name("상품2")
            .registeredAt(FIXED_NOW.minusDays(2))
            .endAt(FIXED_NOW.plusDays(2))
            .currentBidPrice(7000L)
            .minBidPrice(1500L)
            .isSold(true)
            .build();

        Page<SearchProductInfoResponse> responsePage = new PageImpl<>(List.of(resp), PageRequest.of(0, 5), 1);
        given(searchingMapper.toResponse(eq(projectionPage))).willReturn(responsePage);

        Page<SearchProductInfoResponse> result = searchingInternalService.searchProduct(request);

        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getId()).isEqualTo(2L);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        ArgumentCaptor<LocalDateTime> endAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(searchingQueryRepository).findProductsWithFilters(
            pageableCaptor.capture(),
            eq(2L),
            endAtCaptor.capture(),
            eq(true),
            eq("someSort"),
            eq("abc"));

        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
        assertThat(capturedPageable.getPageSize()).isEqualTo(5);

        assertThat(endAtCaptor.getValue()).isNull();
    }
}

package org.oneog.uppick.domain.searching.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.searching.dto.projection.SearchProductProjection;
import org.oneog.uppick.domain.searching.dto.response.SearchProductInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public class SearchingMapperTest {

    private SearchingMapper searchingMapper;

    @BeforeEach
    void setUp() {
        searchingMapper = new SearchingMapper();
    }

    @Test
    void toResponse_프로젝션페이지가주어지면_응답페이지로변환한다() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        SearchProductProjection p1 = new SearchProductProjection(
            1L,
            "image1.jpg",
            "상품1",
            now.minusDays(1),
            now.plusDays(6),
            5000L,
            1000L,
            false);

        SearchProductProjection p2 = new SearchProductProjection(
            2L,
            "image2.jpg",
            "상품2",
            now.minusDays(2),
            now.plusDays(5),
            null,
            2000L,
            true);

        Page<SearchProductProjection> projections = new PageImpl<>(List.of(p1, p2), PageRequest.of(0, 10), 2);

        // When
        Page<SearchProductInfoResponse> responses = searchingMapper.toResponse(projections);

        // Then
        assertEquals(2, responses.getTotalElements());

        SearchProductInfoResponse r1 = responses.getContent().get(0);
        assertEquals(1L, r1.getId());
        assertEquals("image1.jpg", r1.getImage());
        assertEquals("상품1", r1.getName());

        SearchProductInfoResponse r2 = responses.getContent().get(1);
        assertEquals(2L, r2.getId());
        assertEquals("image2.jpg", r2.getImage());
        assertEquals("상품2", r2.getName());
    }
}

package org.oneog.uppick.domain.searching.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchProductProjection {
    private String image;
    private String name;
    private LocalDateTime registeredAt;
    private LocalDateTime endAt;
    private Long currentPrice;
    private Long minPrice;
    private Boolean isSold;
}
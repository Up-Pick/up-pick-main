package org.oneog.uppick.domain.searching.dto.projection;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchProductProjection {
    private long id;
    private String image;
    private String name;
    private LocalDateTime registeredAt;
    private LocalDateTime endAt;
    private Long currentBidPrice;
    private long minBidPrice;
    private boolean isSold;
}
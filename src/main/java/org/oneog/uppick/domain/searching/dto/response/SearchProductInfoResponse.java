package org.oneog.uppick.domain.searching.dto.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SearchProductInfoResponse {
    private long id;
    private String image;
    private String name;
    private LocalDateTime registeredAt;
    private LocalDateTime endAt;
    private Long currentBidPrice;
    private long minBidPrice;
    private boolean isSold;
}

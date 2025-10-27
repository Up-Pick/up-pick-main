package org.oneog.uppick.domain.searching.mapper;

import org.oneog.uppick.domain.searching.dto.projection.SearchProductProjection;
import org.oneog.uppick.domain.searching.dto.response.SearchProductInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class SearchingMapper {

    public Page<SearchProductInfoResponse> toResponse(Page<SearchProductProjection> productProjections) {
        return productProjections.map(projection -> {
            return SearchProductInfoResponse.builder()
                .id(projection.getId())
                .image(projection.getImage())
                .name(projection.getName())
                .registeredAt(projection.getRegisteredAt())
                .endAt(projection.getEndAt())
                .currentBidPrice(projection.getCurrentBidPrice())
                .minBidPrice(projection.getMinBidPrice())
                .isSold(projection.isSold())
                .build();
        });
    }
}

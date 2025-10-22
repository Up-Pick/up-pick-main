package org.oneog.uppick.product.domain.auction;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class AuctionServiceLocalProvider implements AuctionExternalServiceApi {

    @Override
    public void registerAuction(Long id, Long startBid, LocalDateTime registeredAt,
        LocalDateTime endAt) {
        // 로컬 환경에서는 별도의 처리가 필요 없으므로 빈 구현체로 둠
        return;
    }
}

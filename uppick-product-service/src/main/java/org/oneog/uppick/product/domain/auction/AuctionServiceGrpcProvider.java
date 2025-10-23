package org.oneog.uppick.product.domain.auction;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.oneog.uppick.proto.auction.AuctionServiceGrpc;
import org.oneog.uppick.proto.auction.RegisterAuctionRequest;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.google.protobuf.Timestamp;

import lombok.RequiredArgsConstructor;

@Component
@Primary
@RequiredArgsConstructor
@Profile({"dev", "local", "prod"})
public class AuctionServiceGrpcProvider implements AuctionServiceApi {

    private final AuctionServiceGrpc.AuctionServiceBlockingStub mainGrpcClient;

    @Override
    public void registerAuction(Long id, Long startBid, LocalDateTime registeredAt,
        LocalDateTime endAt) {
        RegisterAuctionRequest request = RegisterAuctionRequest.newBuilder()
            .setProductId(id)
            .setMinPrice(startBid)
            .setRegisteredAt(Timestamp.newBuilder().setSeconds(registeredAt.toEpochSecond(ZoneOffset.UTC))
                .setNanos(registeredAt.getNano()).build())
            .setEndAt(Timestamp.newBuilder().setSeconds(endAt.toEpochSecond(ZoneOffset.UTC)).setNanos(endAt.getNano())
                .build())
            .build();
    }
}

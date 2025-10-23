package org.oneog.uppick.product.common.config;

import org.oneog.uppick.proto.auction.AuctionServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.devh.boot.grpc.client.inject.GrpcClient;

@Configuration
public class GrpcConfig {
    @GrpcClient("main-service")
    private AuctionServiceGrpc.AuctionServiceBlockingStub grpcClient;

    @Bean
    public AuctionServiceGrpc.AuctionServiceBlockingStub getAuctionGrpcClient() {
        return grpcClient;
    }
}

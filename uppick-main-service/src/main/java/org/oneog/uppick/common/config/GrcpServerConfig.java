package org.oneog.uppick.common.config;

import org.oneog.uppick.domain.auction.service.AuctionGrpcServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.grpc.Server;
import io.grpc.ServerBuilder;

@Configuration
public class GrcpServerConfig {
    @Bean
    public Server grpcServer(@Value("${grpc.server.port}")
    int port, AuctionGrpcServer server) throws Exception {
        return ServerBuilder.forPort(port)
            .addService(server)
            .build()
            .start();
    }
}

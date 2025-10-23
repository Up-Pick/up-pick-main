package org.oneog.uppick.domain.auction.service;

import java.time.LocalDateTime;

import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.proto.auction.AuctionServiceGrpc;
import org.oneog.uppick.proto.auction.RegisterAuctionRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import com.google.protobuf.Empty;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Profile({"dev", "local", "prod"})
public class AuctionGrpcServer extends AuctionServiceGrpc.AuctionServiceImplBase {

	private final AuctionRepository auctionRepository;

	@Override
	@Transactional
	public void registerAuction(RegisterAuctionRequest request, StreamObserver<Empty> responseObserver) {
		Auction auction = Auction.builder()
			.productId(request.getProductId())
			.minPrice(request.getMinPrice())
			.startAt(LocalDateTime.ofInstant(
				java.time.Instant.ofEpochSecond(
					request.getRegisteredAt().getSeconds(),
					request.getRegisteredAt().getNanos()),
				java.time.ZoneId.systemDefault()))
			.endAt(LocalDateTime.ofInstant(
				java.time.Instant.ofEpochSecond(
					request.getEndAt().getSeconds(),
					request.getEndAt().getNanos()),
				java.time.ZoneId.systemDefault()))
			.build();
		auctionRepository.save(auction);

		responseObserver.onNext(Empty.getDefaultInstance());
		responseObserver.onCompleted();
	}
}

package org.oneog.uppick.domain.ranking.repository;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.common.config.JpaAuditingConfig;
import org.oneog.uppick.common.config.QueryDSLConfig;
import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.domain.auction.enums.AuctionStatus;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.domain.auction.repository.BiddingDetailRepository;
import org.oneog.uppick.domain.product.entity.Product;
import org.oneog.uppick.domain.product.repository.ProductRepository;
import org.oneog.uppick.domain.ranking.dto.HotDealCalculationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@DataJpaTest
@Transactional
@Import({QueryDSLConfig.class, RankingQueryRepository.class, JpaAuditingConfig.class})
class RankingQueryRepositoryTest {

	@Autowired
	private EntityManager em;

	@Autowired
	private RankingQueryRepository rankingQueryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private AuctionRepository auctionRepository;

	@Autowired
	private BiddingDetailRepository biddingDetailRepository;

	@BeforeEach
	void setUp() {
		biddingDetailRepository.deleteAll();
		auctionRepository.deleteAll();
		productRepository.deleteAll();
	}

	@Test
	@DisplayName("지난 24시간 입찰 많은 상품 Top 6을 조회한다")
	void findTop6HotDealsByBidCount_기본조회() {
		// given
		Product product1 = createProduct("맥북 프로", "macbook.jpg");
		Product product2 = createProduct("아이폰 15", "iphone.jpg");
		Product product3 = createProduct("에어팟", "airpods.jpg");

		Auction auction1 = createAuction(product1.getId());
		Auction auction2 = createAuction(product2.getId());
		Auction auction3 = createAuction(product3.getId());

		// 상품1: 입찰 3개
		createBidding(auction1.getId(), 100000L);
		createBidding(auction1.getId(), 110000L);
		createBidding(auction1.getId(), 120000L);

		// 상품2: 입찰 2개
		createBidding(auction2.getId(), 80000L);
		createBidding(auction2.getId(), 85000L);

		// 상품3: 입찰 1개
		createBidding(auction3.getId(), 20000L);

		em.flush();
		em.clear();

		// when
		List<HotDealCalculationDto> result = rankingQueryRepository.findTop6HotDealsByBidCount();

		// then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getProductName()).isEqualTo("맥북 프로");
		assertThat(result.get(1).getProductName()).isEqualTo("아이폰 15");
		assertThat(result.get(2).getProductName()).isEqualTo("에어팟");
	}

	@Test
	@DisplayName("입찰이 없는 상품은 조회되지 않는다")
	void findTop6HotDealsByBidCount_입찰없음() {
		// given
		Product product = createProduct("입찰없는상품", "no-bid.jpg");
		createAuction(product.getId());

		em.flush();
		em.clear();

		// when
		List<HotDealCalculationDto> result = rankingQueryRepository.findTop6HotDealsByBidCount();

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("경매가 없는 상품은 조회되지 않는다")
	void findTop6HotDealsByBidCount_경매없음() {
		// given
		createProduct("경매없는상품", "no-auction.jpg");

		em.flush();
		em.clear();

		// when
		List<HotDealCalculationDto> result = rankingQueryRepository.findTop6HotDealsByBidCount();

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("6개를 초과하면 입찰 많은 순으로 6개만 반환한다")
	void findTop6HotDealsByBidCount_6개초과() {
		// given
		for (int i = 1; i <= 10; i++) {
			Product product = createProduct("상품" + i, "image" + i + ".jpg");
			Auction auction = createAuction(product.getId());

			// i번 입찰 생성 (상품10이 입찰 10개로 가장 많음)
			for (int j = 0; j < i; j++) {
				createBidding(auction.getId(), 10000L + j);
			}
		}

		em.flush();
		em.clear();

		// when
		List<HotDealCalculationDto> result = rankingQueryRepository.findTop6HotDealsByBidCount();

		// then
		assertThat(result).hasSize(6);
		assertThat(result.get(0).getProductName()).isEqualTo("상품10");
		assertThat(result.get(5).getProductName()).isEqualTo("상품5");
	}

	@Test
	@DisplayName("24시간 이내 입찰만 카운트한다")
	void findTop6HotDealsByBidCount_24시간필터() throws Exception {
		// given
		Product product = createProduct("테스트상품", "test.jpg");
		Auction auction = createAuction(product.getId());

		// 24시간 이내 입찰 2개
		createBidding(auction.getId(), 10000L);
		createBidding(auction.getId(), 11000L);

		// 24시간 이전 입찰 3개 (리플렉션으로 시간 변경)
		BiddingDetail oldBid1 = createBidding(auction.getId(), 12000L);
		BiddingDetail oldBid2 = createBidding(auction.getId(), 13000L);
		BiddingDetail oldBid3 = createBidding(auction.getId(), 14000L);

		em.flush();

		// 리플렉션으로 bidAt을 2일 전으로 변경
		LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
		setBidAtUsingReflection(oldBid1, twoDaysAgo);
		setBidAtUsingReflection(oldBid2, twoDaysAgo);
		setBidAtUsingReflection(oldBid3, twoDaysAgo);

		em.flush();
		em.clear();

		// when
		List<HotDealCalculationDto> result = rankingQueryRepository.findTop6HotDealsByBidCount();

		// then
		assertThat(result).hasSize(1);
	}

	@Test
	@DisplayName("여러 상품의 입찰을 정확히 집계한다")
	void findTop6HotDealsByBidCount_복잡한시나리오() {
		// given
		Product p1 = createProduct("인기상품", "hot.jpg");
		Product p2 = createProduct("보통상품", "normal.jpg");
		Product p3 = createProduct("신상품", "new.jpg");
		Product p4 = createProduct("입찰없음", "none.jpg");

		Auction a1 = createAuction(p1.getId());
		Auction a2 = createAuction(p2.getId());
		Auction a3 = createAuction(p3.getId());
		createAuction(p4.getId());  // 입찰 없음

		// 인기상품: 5개
		for (int i = 0; i < 5; i++) {
			createBidding(a1.getId(), 10000L + i * 1000);
		}

		// 보통상품: 3개
		for (int i = 0; i < 3; i++) {
			createBidding(a2.getId(), 20000L + i * 1000);
		}

		// 신상품: 1개
		createBidding(a3.getId(), 30000L);

		em.flush();
		em.clear();

		// when
		List<HotDealCalculationDto> result = rankingQueryRepository.findTop6HotDealsByBidCount();

		// then
		assertThat(result).hasSize(3);

		assertThat(result.get(0).getProductName()).isEqualTo("인기상품");
		assertThat(result.get(0).getProductImage()).isEqualTo("hot.jpg");

		assertThat(result.get(1).getProductName()).isEqualTo("보통상품");

		assertThat(result.get(2).getProductName()).isEqualTo("신상품");

		// 입찰없는 상품은 제외
		assertThat(result).extracting("productName")
			.doesNotContain("입찰없음");
	}

	// === Helper Methods ===

	private Product createProduct(String name, String image) {
		Product product = Product.builder()
			.name(name)
			.description("설명")
			.image(image)
			.categoryId(1L)
			.registerId(1L)
			.build();
		return productRepository.save(product);
	}

	private Auction createAuction(Long productId) {
		Auction auction = Auction.builder()
			.productId(productId)
			.minPrice(10000L)
			.currentPrice(10000L)
			.status(AuctionStatus.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(7))
			.build();
		return auctionRepository.save(auction);
	}

	private BiddingDetail createBidding(Long auctionId, Long bidPrice) {
		BiddingDetail bidding = BiddingDetail.builder()
			.auctionId(auctionId)
			.memberId(1L)
			.bidPrice(bidPrice)
			.build();
		return biddingDetailRepository.save(bidding);
	}

	private void setBidAtUsingReflection(BiddingDetail biddingDetail, LocalDateTime dateTime) throws Exception {
		Field bidAtField = BiddingDetail.class.getDeclaredField("bidAt");
		bidAtField.setAccessible(true);
		bidAtField.set(biddingDetail, dateTime);
	}
}
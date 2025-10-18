package org.oneog.uppick.domain.ranking.repository;

import static org.assertj.core.api.Assertions.*;

import java.sql.Timestamp;
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
import org.oneog.uppick.domain.ranking.dto.HotKeywordCalculationDto;
import org.oneog.uppick.domain.searching.entity.SearchHistory;
import org.oneog.uppick.domain.searching.repository.SearchHistoryJpaRepository;
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

	@Autowired
	private SearchHistoryJpaRepository searchHistoryJpaRepository;

	@BeforeEach
	void setUp() {
		biddingDetailRepository.deleteAll();
		auctionRepository.deleteAll();
		productRepository.deleteAll();
		searchHistoryJpaRepository.deleteAll();
	}

	// === 주간 핫딜 조회 ===

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
		Product product2 = createProduct("테스트상품2", "test.jpg");
		Auction auction = createAuction(product.getId());
		Auction auction2 = createAuction(product2.getId());

		// 24시간 이내 입찰 2개
		createBidding(auction.getId(), 10000L);
		createBidding(auction.getId(), 11000L);

		// 24시간 이전 입찰 3개
		BiddingDetail oldBid1 = createBidding(auction2.getId(), 12000L);
		BiddingDetail oldBid2 = createBidding(auction2.getId(), 13000L);
		BiddingDetail oldBid3 = createBidding(auction2.getId(), 14000L);

		em.flush();

		// bidAt을 2일 전으로 변경
		LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
		setBidAtUsingNativeQuery(oldBid1, twoDaysAgo);
		setBidAtUsingNativeQuery(oldBid2, twoDaysAgo);
		setBidAtUsingNativeQuery(oldBid3, twoDaysAgo);

		em.flush();
		em.clear();

		// when
		List<HotDealCalculationDto> result = rankingQueryRepository.findTop6HotDealsByBidCount();
		System.out.println("oldBid1 = " + oldBid1.getBidAt());
		System.out.println("oldBid2 = " + oldBid2.getBidAt());
		System.out.println("oldBid3 = " + oldBid3.getBidAt());
		BiddingDetail check1 = biddingDetailRepository.findById(oldBid1.getId()).get();
		System.out.println("check1 = " + check1.getBidAt());

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

	// === 주간 키워드 조회 ===

	@Test
	@DisplayName("지난 7일간 검색 많은 키워드 Top 10을 조회한다")
	void findTop10HotKeywordsByCount_기본조회() {
		// given
		// 키워드별 검색 횟수
		createSearchHistory("맥북");    // 5회
		createSearchHistory("맥북");
		createSearchHistory("맥북");
		createSearchHistory("맥북");
		createSearchHistory("맥북");

		createSearchHistory("아이폰");  // 3회
		createSearchHistory("아이폰");
		createSearchHistory("아이폰");

		createSearchHistory("에어팟");  // 2회
		createSearchHistory("에어팟");

		createSearchHistory("아이패드"); // 1회

		em.flush();
		em.clear();

		// when
		List<HotKeywordCalculationDto> result = rankingQueryRepository.findTop10HotKeywordsByCount();

		// then
		assertThat(result).hasSize(4);
		assertThat(result.get(0).getKeyword()).isEqualTo("맥북");
		assertThat(result.get(1).getKeyword()).isEqualTo("아이폰");
		assertThat(result.get(2).getKeyword()).isEqualTo("에어팟");
		assertThat(result.get(3).getKeyword()).isEqualTo("아이패드");
	}

	@Test
	@DisplayName("10개를 초과하면 상위 10개만 반환한다")
	void findTop10HotKeywordsByCount_10개초과() {
		// given
		for (int i = 1; i <= 15; i++) {
			String keyword = "키워드" + i;
			// i번 검색 (키워드15가 15회로 가장 많음)
			for (int j = 0; j < i; j++) {
				createSearchHistory(keyword);
			}
		}

		em.flush();
		em.clear();

		// when
		List<HotKeywordCalculationDto> result = rankingQueryRepository.findTop10HotKeywordsByCount();

		// then
		assertThat(result).hasSize(10);
		assertThat(result.get(0).getKeyword()).isEqualTo("키워드15");
		assertThat(result.get(9).getKeyword()).isEqualTo("키워드6");

		// 키워드5 이하는 제외
		assertThat(result).extracting("keyword")
			.doesNotContain("키워드5", "키워드4", "키워드3");
	}

	@Test
	@DisplayName("검색 기록이 없으면 빈 리스트를 반환한다")
	void findTop10HotKeywordsByCount_검색없음() {
		// given - 검색 기록 없음

		// when
		List<HotKeywordCalculationDto> result = rankingQueryRepository.findTop10HotKeywordsByCount();

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("7일 이전 검색은 제외된다")
	void findTop10HotKeywordsByCount_7일이전제외() throws Exception {
		// given
		// 7일 이내 검색 3개
		createSearchHistory("최신키워드");
		createSearchHistory("최신키워드");
		createSearchHistory("최신키워드");

		// 8일 전 검색 5개
		SearchHistory old1 = createSearchHistory("옛날키워드");
		SearchHistory old2 = createSearchHistory("옛날키워드");
		SearchHistory old3 = createSearchHistory("옛날키워드");
		SearchHistory old4 = createSearchHistory("옛날키워드");
		SearchHistory old5 = createSearchHistory("옛날키워드");

		em.flush();

		// 8일 전으로 변경
		LocalDateTime eightDaysAgo = LocalDateTime.now().minusDays(8);
		setSearchedAtUsingNativeQuery(old1, eightDaysAgo);
		setSearchedAtUsingNativeQuery(old2, eightDaysAgo);
		setSearchedAtUsingNativeQuery(old3, eightDaysAgo);
		setSearchedAtUsingNativeQuery(old4, eightDaysAgo);
		setSearchedAtUsingNativeQuery(old5, eightDaysAgo);

		em.flush();
		em.clear();

		// when
		List<HotKeywordCalculationDto> result = rankingQueryRepository.findTop10HotKeywordsByCount();

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getKeyword()).isEqualTo("최신키워드");

		// 8일 전 데이터는 제외
		assertThat(result).extracting("keyword")
			.doesNotContain("옛날키워드");
	}

	@Test
	@DisplayName("대소문자가 다르면 다른 키워드로 집계된다")
	void findTop10HotKeywordsByCount_대소문자구분() {
		// given
		createSearchHistory("Apple");
		createSearchHistory("Apple");
		createSearchHistory("apple");
		createSearchHistory("APPLE");

		em.flush();
		em.clear();

		// when
		List<HotKeywordCalculationDto> result = rankingQueryRepository.findTop10HotKeywordsByCount();

		// then
		assertThat(result).hasSize(3);
		assertThat(result).extracting("keyword")
			.containsExactlyInAnyOrder("Apple", "apple", "APPLE");
	}

	// === 핫딜 Helper Methods ===

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
			.startAt(LocalDateTime.now())
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

	private void setBidAtUsingNativeQuery(BiddingDetail biddingDetail, LocalDateTime dateTime) {
		em.createNativeQuery(
				"UPDATE bidding_detail SET bid_at = ? WHERE id = ?")
			.setParameter(1, Timestamp.valueOf(dateTime))
			.setParameter(2, biddingDetail.getId())
			.executeUpdate();
	}

	// === 키워드 Helper Methods ===

	private SearchHistory createSearchHistory(String keyword) {
		SearchHistory searchHistory = new SearchHistory(keyword);
		return searchHistoryJpaRepository.save(searchHistory);
	}

	private void setSearchedAtUsingNativeQuery(SearchHistory searchHistory, LocalDateTime dateTime) {
		em.createNativeQuery(
				"UPDATE search_history SET searched_at = ? WHERE id = ?")
			.setParameter(1, Timestamp.valueOf(dateTime))
			.setParameter(2, searchHistory.getId())
			.executeUpdate();
	}
}
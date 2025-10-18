package org.oneog.uppick.domain.ranking.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.entity.BiddingDetail;
import org.oneog.uppick.domain.auction.enums.AuctionStatus;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.domain.auction.repository.BiddingDetailRepository;
import org.oneog.uppick.domain.product.entity.Product;
import org.oneog.uppick.domain.product.repository.ProductRepository;
import org.oneog.uppick.domain.ranking.entity.HotDeal;
import org.oneog.uppick.domain.ranking.entity.HotKeyword;
import org.oneog.uppick.domain.ranking.repository.HotDealRepository;
import org.oneog.uppick.domain.ranking.repository.HotKeywordRepository;
import org.oneog.uppick.domain.searching.entity.SearchHistory;
import org.oneog.uppick.domain.searching.repository.SearchHistoryJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RankingSchedulerServiceTest {

	@Autowired
	private RankingSchedulerService rankingSchedulerService;

	@Autowired
	private HotDealRepository hotDealRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private AuctionRepository auctionRepository;

	@Autowired
	private BiddingDetailRepository biddingDetailRepository;

	@Autowired
	private HotKeywordRepository hotKeywordRepository;

	@Autowired
	private SearchHistoryJpaRepository searchHistoryJpaRepository;

	@BeforeEach
	void setUp() {
		hotDealRepository.deleteAll();
		biddingDetailRepository.deleteAll();
		auctionRepository.deleteAll();
		productRepository.deleteAll();
		hotKeywordRepository.deleteAll();
		searchHistoryJpaRepository.deleteAll();
	}

	// === 주간 핫딜 조회 ===

	@Test
	@DisplayName("핫딜 랭킹이 정상적으로 업데이트된다")
	void updateDailyTop6HotDeals_정상동작() {
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

		// when
		rankingSchedulerService.updateDailyTop6HotDeals();

		// then
		List<HotDeal> hotDeals = hotDealRepository.findAll();
		assertThat(hotDeals).hasSize(3);

		// rankNo로 정렬
		hotDeals.sort(Comparator.comparing(HotDeal::getRankNo));

		assertThat(hotDeals.get(0).getRankNo()).isEqualTo(1);
		assertThat(hotDeals.get(0).getProductName()).isEqualTo("맥북 프로");
		assertThat(hotDeals.get(0).getProductImage()).isEqualTo("macbook.jpg");
		assertThat(hotDeals.get(0).getProductId()).isEqualTo(product1.getId());

		assertThat(hotDeals.get(1).getRankNo()).isEqualTo(2);
		assertThat(hotDeals.get(1).getProductName()).isEqualTo("아이폰 15");

		assertThat(hotDeals.get(2).getRankNo()).isEqualTo(3);
		assertThat(hotDeals.get(2).getProductName()).isEqualTo("에어팟");
	}

	@Test
	@DisplayName("기존 핫딜 데이터가 삭제되고 새로운 데이터가 저장된다")
	void updateDailyTop6HotDeals_기존데이터삭제() {
		// given
		// 기존 핫딜 데이터
		HotDeal oldHotDeal = HotDeal.builder()
			.rankNo(1)
			.productId(999L)
			.productName("옛날 상품")
			.productImage("old.jpg")
			.build();
		hotDealRepository.save(oldHotDeal);

		assertThat(hotDealRepository.findAll()).hasSize(1);

		// 새로운 상품 데이터(상품에 새로운 입찰이 생김)
		Product product = createProduct("새 상품", "new.jpg");
		Auction auction = createAuction(product.getId());
		createBidding(auction.getId(), 10000L);

		// when
		//기존에 저장된 HotDeal이 지워지고 새 상품 데이터로 핫딜 생성
		rankingSchedulerService.updateDailyTop6HotDeals();

		// then
		List<HotDeal> hotDeals = hotDealRepository.findAll();
		assertThat(hotDeals).hasSize(1);
		assertThat(hotDeals.get(0).getProductName()).isEqualTo("새 상품");
		assertThat(hotDeals.get(0).getProductImage()).isEqualTo("new.jpg");

		// 기존 데이터는 없어야 함
		assertThat(hotDeals).extracting("productName").doesNotContain("옛날 상품");
	}

	@Test
	@DisplayName("입찰 데이터가 없으면 핫딜 테이블이 비워진다")
	void updateDailyTop6HotDeals_입찰없음() {
		// given
		Product product = createProduct("상품", "image.jpg");
		createAuction(product.getId());
		// 입찰 없음!

		// when
		rankingSchedulerService.updateDailyTop6HotDeals();

		// then
		assertThat(hotDealRepository.findAll()).isEmpty();
	}

	@Test
	@DisplayName("입찰이 없으면 기존 핫딜도 함께 삭제된다")
	void updateDailyTop6HotDeals_입찰없으면_기존핫딜도삭제() {
		// given
		// 기존 핫딜 저장
		HotDeal existingHotDeal = HotDeal.builder()
			.rankNo(1)
			.productId(100L)
			.productName("어제의 인기상품")
			.productImage("yesterday.jpg")
			.build();
		hotDealRepository.save(existingHotDeal);

		// 오늘은 입찰 없음
		Product product = createProduct("오늘상품", "today.jpg");
		createAuction(product.getId());

		// when
		rankingSchedulerService.updateDailyTop6HotDeals();

		// then
		assertThat(hotDealRepository.findAll()).isEmpty();
	}

	@Test
	@DisplayName("6개 미만의 상품만 있어도 정상 동작한다")
	void updateDailyTop6HotDeals_6개미만() {
		// given
		Product product1 = createProduct("상품1", "img1.jpg");
		Product product2 = createProduct("상품2", "img2.jpg");

		Auction auction1 = createAuction(product1.getId());
		Auction auction2 = createAuction(product2.getId());

		createBidding(auction1.getId(), 10000L);
		createBidding(auction2.getId(), 20000L);

		// when
		rankingSchedulerService.updateDailyTop6HotDeals();

		// then
		List<HotDeal> hotDeals = hotDealRepository.findAll();
		assertThat(hotDeals).hasSize(2);
		assertThat(hotDeals).extracting("rankNo")
			.containsExactlyInAnyOrder(1, 2);
	}

	@Test
	@DisplayName("6개를 초과하면 상위 6개만 저장된다")
	void updateDailyTop6HotDeals_6개초과() {
		// given
		for (int i = 1; i <= 10; i++) {
			Product product = createProduct("상품" + i, "image" + i + ".jpg");
			Auction auction = createAuction(product.getId());

			// i번 입찰 (상품10이 10개로 가장 많음)
			for (int j = 0; j < i; j++) {
				createBidding(auction.getId(), 10000L + j);
			}
		}

		// when
		rankingSchedulerService.updateDailyTop6HotDeals();

		// then
		List<HotDeal> hotDeals = hotDealRepository.findAll();
		hotDeals.sort(Comparator.comparing(HotDeal::getRankNo));
		assertThat(hotDeals).hasSize(6);

		// 1위는 상품10 (입찰 10개)
		assertThat(hotDeals.get(0).getRankNo()).isEqualTo(1);
		assertThat(hotDeals.get(0).getProductName()).isEqualTo("상품10");

		// 6위는 상품5 (입찰 5개)
		assertThat(hotDeals.get(5).getRankNo()).isEqualTo(6);
		assertThat(hotDeals.get(5).getProductName()).isEqualTo("상품5");

		// 상품4 이하는 없어야 함
		assertThat(hotDeals).extracting("productName").doesNotContain("상품4");
	}

	// === 주간 키워드 조회 ===

	@Test
	@DisplayName("주간 핫 키워드가 정상적으로 업데이트된다")
	void updateWeeklyTop10HotKeywords_정상동작() {
		// given
		createSearchHistory("맥북", 5);
		createSearchHistory("아이폰", 3);
		createSearchHistory("에어팟", 2);
		createSearchHistory("아이패드", 1);

		// when
		rankingSchedulerService.updateWeeklyTop10HotKeywords();

		// then
		List<HotKeyword> hotKeywords = hotKeywordRepository.findAll();
		hotKeywords.sort(Comparator.comparing(HotKeyword::getRankNo));

		assertThat(hotKeywords).hasSize(4);
		assertThat(hotKeywords.get(0).getRankNo()).isEqualTo(1);
		assertThat(hotKeywords.get(0).getKeyword()).isEqualTo("맥북");
		assertThat(hotKeywords.get(1).getRankNo()).isEqualTo(2);
		assertThat(hotKeywords.get(1).getKeyword()).isEqualTo("아이폰");
		assertThat(hotKeywords.get(2).getRankNo()).isEqualTo(3);
		assertThat(hotKeywords.get(2).getKeyword()).isEqualTo("에어팟");
		assertThat(hotKeywords.get(3).getRankNo()).isEqualTo(4);
		assertThat(hotKeywords.get(3).getKeyword()).isEqualTo("아이패드");
	}

	@Test
	@DisplayName("기존 핫 키워드 데이터가 삭제되고 새로운 데이터가 저장된다")
	void updateWeeklyTop10HotKeywords_기존데이터삭제() {
		// given
		// 기존 핫 키워드
		HotKeyword oldKeyword = new HotKeyword("옛날키워드", 1);
		hotKeywordRepository.save(oldKeyword);

		assertThat(hotKeywordRepository.findAll()).hasSize(1);

		// 새로운 검색 데이터
		createSearchHistory("새키워드", 3);

		// when
		rankingSchedulerService.updateWeeklyTop10HotKeywords();

		// then
		List<HotKeyword> hotKeywords = hotKeywordRepository.findAll();
		assertThat(hotKeywords).hasSize(1);
		assertThat(hotKeywords.get(0).getKeyword()).isEqualTo("새키워드");

		// 기존 데이터 없음
		assertThat(hotKeywords).extracting("keyword")
			.doesNotContain("옛날키워드");
	}

	@Test
	@DisplayName("검색 기록이 없으면 핫 키워드 테이블이 비워진다")
	void updateWeeklyTop10HotKeywords_검색없음() {
		// given - 검색 기록 없음

		// when
		rankingSchedulerService.updateWeeklyTop10HotKeywords();

		// then
		assertThat(hotKeywordRepository.findAll()).isEmpty();
	}

	@Test
	@DisplayName("검색이 없으면 기존 핫 키워드도 함께 삭제된다")
	void updateWeeklyTop10HotKeywords_검색없으면_기존핫키워드삭제() {
		// given
		HotKeyword existingKeyword = new HotKeyword("기존키워드", 1);
		hotKeywordRepository.save(existingKeyword);

		// 검색 기록 없음

		// when
		rankingSchedulerService.updateWeeklyTop10HotKeywords();

		// then
		assertThat(hotKeywordRepository.findAll()).isEmpty();
	}

	@Test
	@DisplayName("10개를 초과하면 상위 10개만 저장된다")
	void updateWeeklyTop10HotKeywords_10개초과() {
		// given
		for (int i = 1; i <= 15; i++) {
			createSearchHistory("키워드" + i, i); // 키워드15가 15회로 가장 많음
		}

		// when
		rankingSchedulerService.updateWeeklyTop10HotKeywords();

		// then
		List<HotKeyword> hotKeywords = hotKeywordRepository.findAll();
		hotKeywords.sort(Comparator.comparing(HotKeyword::getRankNo));

		assertThat(hotKeywords).hasSize(10);
		assertThat(hotKeywords.get(0).getRankNo()).isEqualTo(1);
		assertThat(hotKeywords.get(0).getKeyword()).isEqualTo("키워드15");
		assertThat(hotKeywords.get(9).getRankNo()).isEqualTo(10);
		assertThat(hotKeywords.get(9).getKeyword()).isEqualTo("키워드6");

		// 키워드5 이하는 제외
		assertThat(hotKeywords).extracting("keyword")
			.doesNotContain("키워드5", "키워드4");
	}

	@Test
	@DisplayName("rankNo가 1부터 순서대로 부여된다")
	void updateWeeklyTop10HotKeywords_rankNo순서() {
		// given
		createSearchHistory("키워드A", 3);
		createSearchHistory("키워드B", 2);
		createSearchHistory("키워드C", 1);

		// when
		rankingSchedulerService.updateWeeklyTop10HotKeywords();

		// then
		List<HotKeyword> hotKeywords = hotKeywordRepository.findAll();
		assertThat(hotKeywords).hasSize(3);
		assertThat(hotKeywords).extracting("rankNo")
			.containsExactlyInAnyOrder(1, 2, 3);
	}

	@Test
	@DisplayName("10개 미만의 키워드만 있어도 정상 동작한다")
	void updateWeeklyTop10HotKeywords_10개미만() {
		// given
		createSearchHistory("키워드1", 2);
		createSearchHistory("키워드2", 1);

		// when
		rankingSchedulerService.updateWeeklyTop10HotKeywords();

		// then
		List<HotKeyword> hotKeywords = hotKeywordRepository.findAll();
		assertThat(hotKeywords).hasSize(2);
		assertThat(hotKeywords).extracting("rankNo")
			.containsExactlyInAnyOrder(1, 2);
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

	private void createSearchHistory(String keyword, int count) {
		for (int i = 0; i < count; i++) {
			SearchHistory searchHistory = new SearchHistory(keyword);
			searchHistoryJpaRepository.save(searchHistory);
		}
	}
}
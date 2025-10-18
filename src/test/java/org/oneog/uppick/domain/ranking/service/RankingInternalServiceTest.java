package org.oneog.uppick.domain.ranking.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.domain.ranking.dto.response.HotDealResponse;
import org.oneog.uppick.domain.ranking.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.entity.HotDeal;
import org.oneog.uppick.domain.ranking.entity.HotKeyword;
import org.oneog.uppick.domain.ranking.mapper.RankingMapper;
import org.oneog.uppick.domain.ranking.repository.HotDealRepository;
import org.oneog.uppick.domain.ranking.repository.HotKeywordRepository;

@ExtendWith(MockitoExtension.class)
class RankingInternalServiceTest {

	@Mock
	private HotDealRepository hotDealRepository;

	@Mock
	private HotKeywordRepository hotKeywordRepository;

	@Mock
	private RankingMapper rankingMapper;

	@InjectMocks
	private RankingInternalService rankingInternalService;

	// === 주간 핫딜 조회 ===

	@Test
	@DisplayName("핫딜 Top 6을 랭킹 순서대로 조회한다")
	void getHotDeals_성공() {
		// given
		HotDeal hotDeal1 = createHotDeal(1L, 1, "맥북 프로", "macbook.jpg");
		HotDeal hotDeal2 = createHotDeal(2L, 2, "아이폰 15", "iphone.jpg");
		HotDeal hotDeal3 = createHotDeal(3L, 3, "에어팟", "airpods.jpg");
		HotDeal hotDeal4 = createHotDeal(4L, 4, "갤럭시 워치", "galaxy-watch.jpg");
		HotDeal hotDeal5 = createHotDeal(5L, 5, "아이패드", "ipad.jpg");
		HotDeal hotDeal6 = createHotDeal(6L, 6, "다이슨 청소기", "dyson.jpg");

		List<HotDeal> hotDeals = Arrays.asList(hotDeal1, hotDeal2, hotDeal3, hotDeal4, hotDeal5, hotDeal6);

		HotDealResponse response1 = createResponse(1, 1L, "맥북 프로", "macbook.jpg");
		HotDealResponse response2 = createResponse(2, 2L, "아이폰 15", "iphone.jpg");
		HotDealResponse response3 = createResponse(3, 3L, "에어팟", "airpods.jpg");
		HotDealResponse response4 = createResponse(4, 4L, "갤럭시 워치", "galaxy-watch.jpg");
		HotDealResponse response5 = createResponse(5, 5L, "아이패드", "ipad.jpg");
		HotDealResponse response6 = createResponse(6, 6L, "다이슨 청소기", "dyson.jpg");

		given(hotDealRepository.findAllByOrderByRankNoAsc()).willReturn(hotDeals);
		given(rankingMapper.toResponse(hotDeal1)).willReturn(response1);
		given(rankingMapper.toResponse(hotDeal2)).willReturn(response2);
		given(rankingMapper.toResponse(hotDeal3)).willReturn(response3);
		given(rankingMapper.toResponse(hotDeal4)).willReturn(response4);
		given(rankingMapper.toResponse(hotDeal5)).willReturn(response5);
		given(rankingMapper.toResponse(hotDeal6)).willReturn(response6);

		// when
		List<HotDealResponse> result = rankingInternalService.getHotDeals();

		// then
		assertThat(result).hasSize(6);
		assertThat(result.get(0).getRank()).isEqualTo(1);
		assertThat(result.get(0).getProductName()).isEqualTo("맥북 프로");
		assertThat(result.get(1).getRank()).isEqualTo(2);
		assertThat(result.get(2).getRank()).isEqualTo(3);
		assertThat(result.get(3).getRank()).isEqualTo(4);
		assertThat(result.get(4).getRank()).isEqualTo(5);
		assertThat(result.get(5).getRank()).isEqualTo(6);
		assertThat(result.get(5).getProductName()).isEqualTo("다이슨 청소기");
	}

	@Test
	@DisplayName("6개 미만의 핫딜도 정상 조회된다")
	void getHotDeals_6개미만() {
		// given
		HotDeal hotDeal1 = createHotDeal(1L, 1, "상품1", "img1.jpg");
		HotDeal hotDeal2 = createHotDeal(2L, 2, "상품2", "img2.jpg");

		HotDealResponse response1 = createResponse(1, 1L, "상품1", "img1.jpg");
		HotDealResponse response2 = createResponse(2, 2L, "상품2", "img2.jpg");

		given(hotDealRepository.findAllByOrderByRankNoAsc()).willReturn(Arrays.asList(hotDeal1, hotDeal2));
		given(rankingMapper.toResponse(hotDeal1)).willReturn(response1);
		given(rankingMapper.toResponse(hotDeal2)).willReturn(response2);

		// when
		List<HotDealResponse> result = rankingInternalService.getHotDeals();

		// then
		assertThat(result).hasSize(2);
		assertThat(result).extracting("rank").containsExactly(1, 2);
	}

	// === 주간 키워드 조회 ===
	@Test
	@DisplayName("핫 키워드 TOP10을 순위 오름차순으로 조회한다")
	void getHotKeywords_정상조회() {
		// given
		HotKeyword keyword1 = createHotKeyword(1, "맥북");
		HotKeyword keyword2 = createHotKeyword(2, "아이폰");
		HotKeyword keyword3 = createHotKeyword(3, "에어팟");

		List<HotKeyword> keywords = List.of(keyword1, keyword2, keyword3);

		HotKeywordResponse response1 = new HotKeywordResponse("맥북", 1);
		HotKeywordResponse response2 = new HotKeywordResponse("아이폰", 2);
		HotKeywordResponse response3 = new HotKeywordResponse("에어팟", 3);

		given(hotKeywordRepository.findAllByOrderByRankNoAsc()).willReturn(keywords);
		given(rankingMapper.toResponse(keyword1)).willReturn(response1);
		given(rankingMapper.toResponse(keyword2)).willReturn(response2);
		given(rankingMapper.toResponse(keyword3)).willReturn(response3);

		// when
		List<HotKeywordResponse> result = rankingInternalService.getHotKeywords();

		// then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getRankNo()).isEqualTo(1);
		assertThat(result.get(0).getKeyword()).isEqualTo("맥북");
		assertThat(result.get(1).getRankNo()).isEqualTo(2);
		assertThat(result.get(2).getRankNo()).isEqualTo(3);

		// 검증
		verify(hotKeywordRepository, times(1)).findAllByOrderByRankNoAsc();
		verify(rankingMapper, times(3)).toResponse(any(HotKeyword.class));
	}

	@Test
	@DisplayName("핫 키워드가 없으면 빈 리스트를 반환한다")
	void getHotKeywords_빈리스트() {
		// given
		given(hotKeywordRepository.findAllByOrderByRankNoAsc()).willReturn(Collections.emptyList());

		// when
		List<HotKeywordResponse> result = rankingInternalService.getHotKeywords();

		// then
		assertThat(result).isEmpty();
		verify(hotKeywordRepository, times(1)).findAllByOrderByRankNoAsc();
	}

	// === Helper Methods ===

	private HotDeal createHotDeal(Long productId, int rankNo, String name, String image) {
		return HotDeal.builder()
			.productId(productId)
			.rankNo(rankNo)
			.productName(name)
			.productImage(image)
			.build();
	}

	private HotDealResponse createResponse(int rank, Long productId, String name, String image) {
		return HotDealResponse.builder()
			.rank(rank)
			.productId(productId)
			.productName(name)
			.productImage(image)
			.build();
	}

	private HotKeyword createHotKeyword(int rankNo, String keyword) {
		return new HotKeyword(keyword, rankNo);
	}
}
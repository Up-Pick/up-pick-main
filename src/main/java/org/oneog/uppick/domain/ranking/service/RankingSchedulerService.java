package org.oneog.uppick.domain.ranking.service;

import java.util.List;

import org.oneog.uppick.domain.ranking.dto.HotDealCalculationDto;
import org.oneog.uppick.domain.ranking.entity.HotDeal;
import org.oneog.uppick.domain.ranking.repository.HotDealRepository;
import org.oneog.uppick.domain.ranking.repository.RankingQueryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingSchedulerService {

	private final HotDealRepository hotDealRepository;
	private final RankingQueryRepository rankingQueryRepository;

	@Scheduled(cron = "0 0 0 * * *") // 매일 자정 업데이트
	@Transactional
	public void updateDailyTop6HotDeals() {

		//이틀 전 내용 삭제
		hotDealRepository.deleteAll();

		//핫딜 랭킹 가져오기(상품 id, 상품 이름, 상품 이미지)
		List<HotDealCalculationDto> rankings = rankingQueryRepository.findTop6HotDealsByBidCount();

		//핫딜 랭킹 rankNo 설정 및 저장
		for (int i = 0; i < rankings.size(); i++) {
			HotDealCalculationDto dto = rankings.get(i);
			HotDeal hotDeal = HotDeal.builder()
				.rankNo(i + 1)
				.productId(dto.getProductId())
				.productName(dto.getProductName())
				.productImage(dto.getProductImage())
				.build();
			hotDealRepository.save(hotDeal);
		}

	}
}

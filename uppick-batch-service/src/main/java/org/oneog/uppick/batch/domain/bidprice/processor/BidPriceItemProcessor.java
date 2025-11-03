package org.oneog.uppick.batch.domain.bidprice.processor;

import org.oneog.uppick.batch.domain.bidprice.dto.BidPriceDto;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 입찰가 배치 ItemProcessor
 *
 * 데이터 검증 및 변환
 */
@Slf4j
@Component
public class BidPriceItemProcessor implements ItemProcessor<BidPriceDto, BidPriceDto> {

	@Override
	public BidPriceDto process(BidPriceDto item) throws Exception {

		// 데이터 검증
		if (item.getAuctionId() == null || item.getProductId() == null || item.getBidPrice() == null) {
			log.warn("유효하지 않은 입찰가 데이터: {}", item);
			return null; // null 반환 시 해당 아이템은 스킵됨
		}

		if (item.getBidPrice() <= 0) {
			log.debug("입찰가가 0 이하인 데이터 스킵: {}", item);
			return null;
		}

		log.debug("입찰가 데이터 검증 완료 - {}", item);
		return item;
	}

}

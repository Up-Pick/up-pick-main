package org.oneog.uppick.batch.domain.productviewcount.processor;

import org.oneog.uppick.batch.domain.productviewcount.dto.ViewCountDto;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 조회수 배치 ItemProcessor
 *
 * ViewCountData 검증 및 처리
 */
@Slf4j
@Component
public class ViewCountItemProcessor implements ItemProcessor<ViewCountDto, ViewCountDto> {

	@Override
	public ViewCountDto process(ViewCountDto item) throws Exception {

		// 데이터 검증
		if (item.getProductId() == null || item.getProductId() <= 0) {
			log.warn("잘못된 productId: {}, 처리 스킵", item.getProductId());
			return null; // null 반환 시 Writer로 전달되지 않음
		}

		if (item.getViewCount() == null || item.getViewCount() <= 0) {
			log.warn("잘못된 viewCount: {} (productId: {}), 처리 스킵", item.getViewCount(), item.getProductId());
			return null;
		}

		log.debug("조회수 데이터 검증 완료 - {}", item);

		return item;
	}

}

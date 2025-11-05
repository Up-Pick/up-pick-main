package org.oneog.uppick.batch.domain.bidprice.writer;

import org.oneog.uppick.batch.domain.bidprice.dto.BidPriceDto;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 입찰가 배치 ItemWriter
 *
 * Elasticsearch ProductDocument의 current_bid_price 업데이트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BidPriceItemWriter implements ItemWriter<BidPriceDto> {

	private static final String INDEX_NAME = "product";
	private static final String FIELD_CURRENT_BID_PRICE = "current_bid_price";

	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public void write(Chunk<? extends BidPriceDto> chunk) throws Exception {

		log.debug("입찰가 배치 Write 시작 - 처리할 데이터: {}개", chunk.size());

		for (BidPriceDto data : chunk) {
			try {
				// Elasticsearch ProductDocument 업데이트
				UpdateQuery updateQuery = UpdateQuery.builder(String.valueOf(data.getProductId()))
					.withDocument(Document.create()
						.append(FIELD_CURRENT_BID_PRICE, data.getBidPrice()))
					.build();

				elasticsearchOperations.update(updateQuery, IndexCoordinates.of(INDEX_NAME));

				log.debug("productId {}의 입찰가 {}로 ES 업데이트 완료", data.getProductId(), data.getBidPrice());

			} catch (Exception e) {
				log.error("입찰가 ES 업데이트 실패 - auctionId: {}, productId: {}, bidPrice: {}, error: {}",
					data.getAuctionId(), data.getProductId(), data.getBidPrice(), e.getMessage(), e);
				throw e; // Chunk 트랜잭션 롤백
			}
		}

		log.debug("입찰가 배치 Write 완료 - 처리된 데이터: {}개", chunk.size());
	}

}

package org.oneog.uppick.auction.domain.product.command.service;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultProductInnerCommandService implements ProductInnerCommandService {

	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public void markProductAsSold(Long productId) {

		UpdateQuery updateQuery = UpdateQuery.builder(String.valueOf(productId))
			.withDocument(Document.create()
				.append("is_sold", true))
			.build();

		// 명시적으로 인덱스를 지정해 Elasticsearch 매핑 컨텍스트가 UpdateQuery를 엔티티로 해석하는 것을 방지
		elasticsearchOperations.update(updateQuery, IndexCoordinates.of("product"));
	}

}

package org.oneog.uppick.auction.domain.product.command.service;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultProductInnerCommandService implements ProductInnerCommandService {

	private final ElasticsearchOperations elasticsearchOperations;

	@Override
	public void updateProductDocumentStatus(Long productId) {

		UpdateQuery updateQuery = UpdateQuery.builder(String.valueOf(productId))
			.withDocument(Document.create()
				.append("is_sold", true))
			.build();

		elasticsearchOperations.update(updateQuery);
	}

}

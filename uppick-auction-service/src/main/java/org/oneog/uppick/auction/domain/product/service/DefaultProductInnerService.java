package org.oneog.uppick.auction.domain.product.service;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultProductInnerService implements ProductInnerService {

	ElasticsearchOperations elasticsearchOperations;

	@Override
	public void updateProductDocumentStatus(Long productId) {

		UpdateQuery updateQuery = UpdateQuery.builder(String.valueOf(productId))
			.withDocument(Document.create()
				.append("is_sold", false))
			.build();

		elasticsearchOperations.update(updateQuery);
	}

}

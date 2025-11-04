package org.oneog.uppick.auction.domain.product.command.repository;

import org.oneog.uppick.auction.domain.product.common.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, Long> {

}

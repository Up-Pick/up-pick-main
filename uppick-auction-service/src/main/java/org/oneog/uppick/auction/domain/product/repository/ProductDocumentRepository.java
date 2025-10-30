package org.oneog.uppick.auction.domain.product.repository;

import org.oneog.uppick.auction.domain.product.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, Long> {

}

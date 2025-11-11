package org.oneog.uppick.auction.domain.product.query.repository;

import org.oneog.uppick.auction.domain.product.common.document.ProductDocument;
import org.oneog.uppick.auction.domain.product.query.model.dto.request.SearchProductRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ProductESRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchHits<ProductDocument> searchProducts(SearchProductRequest searchProductRequest) {

        NativeQuery query = buildSearchQuery(searchProductRequest);
        return elasticsearchOperations.search(query, ProductDocument.class);
    }

    private NativeQuery buildSearchQuery(SearchProductRequest searchProductRequest) {

        String[] sortType = searchProductRequest.getSortBy().getSortType().split(":");

        return NativeQuery.builder()
            .withQuery(buildBoolQuery(searchProductRequest))
            .withPageable(PageRequest.of(
                searchProductRequest.getPage(),
                searchProductRequest.getSize()))
            .withSort(s -> s
                .field(f -> f
                    .field(sortType[0])
                    .order(sortType[1].equals("asc") ? SortOrder.Asc : SortOrder.Desc)
                    .missing("_last")
                ))
            .build();
    }

    private Query buildBoolQuery(SearchProductRequest searchProductRequest) {

        return Query.of(q -> q
            .bool(b -> {
                if (StringUtils.hasText(searchProductRequest.getKeyword())) {
                    // 상품 이름 : match 조회
                    b.must(m -> m.match(mq -> mq
                        .field("name")
                        .query(searchProductRequest.getKeyword())
                        .fuzziness("AUTO"))
                    );
                }

                // 기본 값 1L 또는 지정된 category_id 조회
                b.filter(f -> f.term(t -> t
                    .field("category_id")
                    .value(searchProductRequest.getCategoryId())
                ));

                // onlyNotSold == false 경우: 판매 안 된 상품만 조회
                if (!searchProductRequest.isOnlyNotSold()) {
                    b.filter(f -> f.term(t -> t
                        .field("is_sold")
                        .value(false)
                    ));
                }

                // 최소 마감 날짜 기준이 null이 아닌 경우 필터링
                if (searchProductRequest.getEndAtFrom() != null) {
                    b.filter(f -> f.range(r -> r
                        .date(d -> d
                            .field("end_at")
                            .gte(searchProductRequest.getEndAtFrom().toString())
                        )
                    ));
                }

                return b;
            })
        );
    }

}

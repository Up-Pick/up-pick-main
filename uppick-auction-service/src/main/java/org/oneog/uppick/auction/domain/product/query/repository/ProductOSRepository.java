package org.oneog.uppick.auction.domain.product.query.repository;

import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.oneog.uppick.auction.domain.product.common.document.ProductDocument;
import org.oneog.uppick.auction.domain.product.query.model.dto.request.SearchProductRequest;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.data.client.osc.NativeQuery;
import org.opensearch.data.client.osc.NativeQueryBuilder;
import org.opensearch.data.core.OpenSearchOperations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class ProductOSRepository {

    private final OpenSearchOperations productSearchOperations;

    public SearchHits<ProductDocument> searchProducts(SearchProductRequest searchProductRequest) {

        NativeQuery query = buildSearchQuery(searchProductRequest);
        return productSearchOperations.search(query, ProductDocument.class);
    }

    private NativeQuery buildSearchQuery(SearchProductRequest request) {

        NativeQueryBuilder queryBuilder = NativeQuery.builder();
        queryBuilder.withQuery(buildBoolQuery(request));
        queryBuilder.withPageable(PageRequest.of(request.getPage(), request.getSize()));
        if (StringUtils.hasText(request.getSortBy().getSortType())) {
            addSort(queryBuilder, request.getSortBy().getSortType());
        }
        return queryBuilder.build();
    }

    private Query buildBoolQuery(SearchProductRequest request) {

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 키워드 검색 (must)
        if (StringUtils.hasText(request.getKeyword())) {
            boolQueryBuilder.must(m -> m
                .match(ma -> ma
                    .field("name")
                    .query(FieldValue.of(request.getKeyword()))
                    .fuzziness("AUTO")
                )
            );
        }

        // 카테고리 필터 (filter)
        boolQueryBuilder.filter(f -> f
            .term(t -> t
                .field("category_id")
                .value(FieldValue.of(request.getCategoryId()))
            )
        );

        // 미판매 상품 필터 (filter)
        if (!request.isOnlyNotSold()) {
            boolQueryBuilder.filter(f -> f
                .term(t -> t
                    .field("is_sold")
                    .value(FieldValue.of(false))
                )
            );
        }

        // 날짜 범위 필터 (filter)
        if (request.getEndAtFrom() != null) {
            boolQueryBuilder.filter(f -> f
                .range(r -> r
                    .field("end_at")
                    .gte(JsonData.of(request.getEndAtFrom().format(DateTimeFormatter.ISO_DATE_TIME)))
                )
            );
        }

        return new Query(boolQueryBuilder.build());
    }

    private void addSort(NativeQueryBuilder queryBuilder, String sortBy) {

        try {
            String[] parts = sortBy.split(":");
            if (parts.length == 2) {
                String field = parts[0];
                SortOrder order = "asc".equalsIgnoreCase(parts[1]) ? SortOrder.Asc : SortOrder.Desc;

                queryBuilder.withSort(s -> s
                    .field(f -> f
                        .field(field)
                        .order(order)
                        .missing(FieldValue.of("_last"))
                    )
                );
            }
        } catch (Exception e) {
        }
    }

}
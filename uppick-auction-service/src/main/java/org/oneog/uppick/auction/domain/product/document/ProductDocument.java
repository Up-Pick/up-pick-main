package org.oneog.uppick.auction.domain.product.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setting(settingPath = "/static/elastic/elastic-settings.json")
@Mapping(mappingPath = "/static/elastic/product-mappings.json")
@Document(indexName = "product")
public class ProductDocument {

	@Id
	private Long id;

	@Field(name = "name", type = FieldType.Text, analyzer = "nori")
	private String name;
	@Field(name = "end_at", type = FieldType.Date, format = DateFormat.date_time)
	private LocalDateTime endAt;
	@Field(name = "category_id", type = FieldType.Long)
	private Long categoryId;
	@Field(name = "is_sold", type = FieldType.Boolean)
	private boolean isSold;

}

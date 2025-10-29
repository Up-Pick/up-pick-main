package org.oneog.uppick.auction.domain.product.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setting(settingPath = "/static/elastic/elastic-settings.json")
@Document(indexName = "product")
public class ProductDocument {

	@Id
	private Long id;

	@Field(name = "name", type = FieldType.Text, analyzer = "nori")
	private String name;
	@Field(name = "image", type = FieldType.Text)
	private String image;

	@Field(name = "registered_at", type = FieldType.Date, format = DateFormat.date_hour_minute, pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime registeredAt;
	@Field(name = "end_at", type = FieldType.Date, format = DateFormat.date_hour_minute, pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime endAt;

	@Field(name = "current_bid_price", type = FieldType.Long)
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private Long currentBidPrice;
	@Field(name = "min_bid_price", type = FieldType.Long)
	private Long minBidPrice;

	@Field(name = "is_sold", type = FieldType.Boolean)
	private boolean isSold;

	@Field(name = "category_id", type = FieldType.Long)
	private Long categoryId;

}

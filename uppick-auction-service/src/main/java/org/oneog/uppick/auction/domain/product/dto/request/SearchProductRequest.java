package org.oneog.uppick.auction.domain.product.dto.request;

import java.time.LocalDate;

import org.oneog.uppick.common.constants.FormatConstants;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchProductRequest {

	private String keyword;
	private Long categoryId;
	private Boolean onlyNotSold;
	private String sortBy;
	private Integer page;
	private Integer size;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FormatConstants.JSON_DATE_FORMAT)
	private LocalDate endAtFrom;

	public static SearchProductRequest ofDefault() {

		return SearchProductRequest.builder()
			.keyword(null)
			.categoryId(null)
			.onlyNotSold(null)
			.sortBy(null)
			.page(0)
			.size(20)
			.endAtFrom(null)
			.build();
	}

}

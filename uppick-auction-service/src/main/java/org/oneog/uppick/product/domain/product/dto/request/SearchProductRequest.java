package org.oneog.uppick.product.domain.product.dto.request;

import java.time.LocalDate;

import org.oneog.uppick.common.constants.FormatConstants;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SearchProductRequest {

	private final String keyword;
	private final Long categoryId;
	private final Boolean onlyNotSold;
	private final String sortBy;
	private final Integer page;
	private final Integer size;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FormatConstants.JSON_DATE_FORMAT)
	private final LocalDate endAtFrom;

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

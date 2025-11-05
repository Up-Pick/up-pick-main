package org.oneog.uppick.auction.domain.product.query.model.dto.request;

import java.time.LocalDate;

import org.oneog.uppick.auction.domain.product.query.controller.enums.ProductSearchSortType;
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

	@Builder.Default
	private long categoryId = 1L;
	@Builder.Default
	private boolean onlyNotSold = false;
	@Builder.Default
	private int page = 0;
	@Builder.Default
	private int size = 20;
	@Builder.Default
	private ProductSearchSortType sortBy = ProductSearchSortType.REGISTERED_AT_DESC;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FormatConstants.JSON_DATE_FORMAT)
	private LocalDate endAtFrom;

	private SearchProductRequest(String keyword, LocalDate endAtFrom) {

		this.keyword = keyword;
		this.endAtFrom = endAtFrom;
	}

	public static SearchProductRequest ofDefault() {

		return new SearchProductRequest(null, null);
	}

}

package org.oneog.uppick.auction.domain.product.dto.request;

import java.time.LocalDate;

import org.oneog.uppick.auction.domain.product.enums.ProductSearchSortType;
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

	private long categoryId = 1L;
	private boolean onlyNotSold = false;
	private int page = 0;
	private int size = 20;

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

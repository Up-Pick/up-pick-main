package org.oneog.uppick.auction.domain.product.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductRegisterRequest {

	@NotBlank(message = "상품 이름을 비울 수 없습니다.")
	private String name;
	@NotBlank(message = "설명을 비울 수 없습니다.")
	private String description;

	@NotNull(message = "카테고리를 비울 수 없습니다.")
	@Positive
	private Long categoryId;

	@NotNull(message = "상품 입찰가를 비울 수 없습니다.")
	@Positive(message = "값을 입력해야 합니다.")
	private Long startBid;

	@NotNull(message = "마감 날짜는 비울 수 없습니다.")
	@Future(message = "마감 날짜는 현재 날짜 이후여야 합니다.")
	private LocalDateTime endAt;

	@AssertTrue(message = "마감 날짜는 현재 날짜로부터 2주 이내여야 합니다.")
	private boolean isEndDateWithinTwoWeeks() {
		if (endAt == null) {
			return true; // @NotNull이 null 체크 담당
		}
		LocalDateTime maxEndDate = LocalDateTime.now().plusDays(14);
		return !endAt.isAfter(maxEndDate);
	}

}

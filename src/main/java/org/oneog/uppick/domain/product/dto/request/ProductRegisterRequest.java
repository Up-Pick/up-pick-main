package org.oneog.uppick.domain.product.dto.request;

import java.time.LocalDateTime;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductRegisterRequest {

	@NotBlank(message = "상품 이름을 비울 수 없습니다.")
	private String name;
	@NotBlank(message = "설명을 비울 수 없습니다.")
	private String description;

	private String image;

	@NotNull(message = "카테고리를 비울 수 없습니다.")
	@Positive
	private Long categoryId;

	@NotNull(message = "상품 입찰가를 비울 수 없습니다.")
	@Positive(message = "값을 입력해야 합니다.")
	private Long startBid;

	@Future(message = "마감 날짜는 현재 날짜 이후여야 합니다.")
	private LocalDateTime endAt;
}

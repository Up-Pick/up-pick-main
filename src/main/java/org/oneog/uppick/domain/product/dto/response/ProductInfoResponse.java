package org.oneog.uppick.domain.product.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
public class ProductInfoResponse {

	private Long id;
	private String name;
	private String description;
	private Long viewCount;
	private LocalDateTime registeredAt;
	private String image;

	private String categoryName;
	private LocalDateTime soldAt;
	private Long currentBid;
	private LocalDateTime endAt;
	private String sellerName;
}
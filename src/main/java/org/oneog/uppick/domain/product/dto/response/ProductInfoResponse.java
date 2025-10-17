package org.oneog.uppick.domain.product.dto.response;

import java.time.LocalDateTime;

import org.oneog.uppick.common.constants.ResponseConstants;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ResponseConstants.JSON_DATE_FORMAT)
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
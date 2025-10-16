package org.oneog.uppick.domain.product.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
public class ProductSoldInfoResponse {
	private Long id;
	private String name;
	private String description;
	private String image;
	private Long finalPrice;
	private LocalDateTime soldAt;
}

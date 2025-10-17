package org.oneog.uppick.domain.product.dto.response;

import java.time.LocalDateTime;

import org.oneog.uppick.common.constants.ResponseConstants;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductPurchasedInfoResponse {
	private Long id;
	private String name;
	private String image;
	private Long finalPrice;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ResponseConstants.JSON_DATE_FORMAT)
	private LocalDateTime buyAt;
}

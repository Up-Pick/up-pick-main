package org.oneog.uppick.domain.member.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PurchasedProductBuyAtResponse {
	private final Long id;
	private final LocalDateTime buyAt;
}

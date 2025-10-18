package org.oneog.uppick.domain.member.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreditChargeRequest {

	@Positive(message = "충전 금액은 0보다 커야 합니다.")//양수인지 확인
	private Long amount;
}

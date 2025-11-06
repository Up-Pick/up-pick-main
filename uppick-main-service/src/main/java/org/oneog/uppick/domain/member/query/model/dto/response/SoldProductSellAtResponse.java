package org.oneog.uppick.domain.member.query.model.dto.response;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SoldProductSellAtResponse {

	private Long id;
	private LocalDateTime soldAt;

}
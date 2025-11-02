package org.oneog.uppick.batch.domain.productviewcount.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 조회수 배치 처리 데이터 DTO
 *
 * Redis에서 읽은 product:view:{productId} 데이터를 담는 객체
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ViewCountDto {

	private Long productId;
	private Long viewCount;

}

package org.oneog.uppick.domain.bid.enums;

import lombok.Getter;

@Getter
public enum Status {
	BEFORE_BID("입찰 진행 전"),
	IN_PROGRESS("입찰 진행 중"),
	FINISHED("입찰 끝남");

	private final String description;

	Status(String description) {
		this.description = description;
	}
}

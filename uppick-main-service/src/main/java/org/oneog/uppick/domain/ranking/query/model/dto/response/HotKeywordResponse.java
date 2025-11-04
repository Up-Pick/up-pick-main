package org.oneog.uppick.domain.ranking.query.model.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class HotKeywordResponse {

	private String keyword;
	private Integer rankNo;

}

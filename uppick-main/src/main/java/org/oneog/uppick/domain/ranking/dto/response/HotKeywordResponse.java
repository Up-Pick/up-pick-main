package org.oneog.uppick.domain.ranking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HotKeywordResponse {

	private final String keyword;

	private final Integer rankNo;
}

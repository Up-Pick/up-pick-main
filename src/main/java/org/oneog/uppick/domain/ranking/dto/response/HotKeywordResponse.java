package org.oneog.uppick.domain.ranking.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HotKeywordResponse {

	String keyword;

	Integer rankNo;
}

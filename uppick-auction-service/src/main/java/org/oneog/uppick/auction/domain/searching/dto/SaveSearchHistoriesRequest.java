package org.oneog.uppick.auction.domain.searching.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SaveSearchHistoriesRequest {

	private List<String> keywords;

}

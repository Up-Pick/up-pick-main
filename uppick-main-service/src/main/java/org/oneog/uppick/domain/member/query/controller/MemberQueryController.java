package org.oneog.uppick.domain.member.query.controller;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.domain.member.query.model.dto.response.CreditGetResponse;
import org.oneog.uppick.domain.member.query.service.MemberQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberQueryController {

	private final MemberQueryService memberQueryService;

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/me/credit")
	public GlobalApiResponse<CreditGetResponse> getCredit(@AuthenticationPrincipal AuthMember authMember) {

		CreditGetResponse response = memberQueryService.getCredit(authMember);
		return GlobalApiResponse.ok(response);
	}

}

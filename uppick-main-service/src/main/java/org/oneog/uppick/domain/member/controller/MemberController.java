package org.oneog.uppick.domain.member.controller;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.domain.member.dto.request.CreditChargeRequest;
import org.oneog.uppick.domain.member.dto.response.CreditChargeResponse;
import org.oneog.uppick.domain.member.dto.response.CreditGetResponse;
import org.oneog.uppick.domain.member.service.MemberService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

	private final MemberService memberService;

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/me/credit/charge")
	public GlobalApiResponse<CreditChargeResponse> creditCharge(
		@Valid @RequestBody
		CreditChargeRequest creditChargeRequest,
		@AuthenticationPrincipal
		AuthMember authMember) {

		CreditChargeResponse response = memberService.chargeCredit(creditChargeRequest, authMember);
		return GlobalApiResponse.ok(response);
	}

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/me/credit")
	public GlobalApiResponse<CreditGetResponse> getCredit(
		@AuthenticationPrincipal
		AuthMember authMember) {

		CreditGetResponse response = memberService.getCredit(authMember);
		return GlobalApiResponse.ok(response);
	}

}

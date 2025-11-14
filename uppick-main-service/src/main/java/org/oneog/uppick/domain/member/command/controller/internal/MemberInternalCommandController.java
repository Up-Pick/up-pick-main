package org.oneog.uppick.domain.member.command.controller.internal;

import org.oneog.uppick.domain.member.command.model.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.domain.member.command.model.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.domain.member.command.model.dto.request.UpdateMemberCreditRequest;
import org.oneog.uppick.domain.member.command.service.MemberInternalCommandService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1")
public class MemberInternalCommandController {

	private final MemberInternalCommandService memberInternalCommandService;

	@PostMapping("/members/{memberId}/credit")
	public void updateMemberCredit(
		@PathVariable long memberId,
		@RequestBody UpdateMemberCreditRequest updateMemberCreditRequest) {

		memberInternalCommandService.updateMemberCredit(memberId, updateMemberCreditRequest.getAmount());
	}

	@PostMapping("/members/purchase-detail")
	public void registerPurchaseDetail(@RequestBody RegisterPurchaseDetailRequest request) {

		memberInternalCommandService.registerPurchaseDetail(request);
	}

	@PostMapping("/members/sell-detail")
	public void registerSellDetail(@RequestBody RegisterSellDetailRequest request) {

		memberInternalCommandService.registerSellDetail(request);

	}

}

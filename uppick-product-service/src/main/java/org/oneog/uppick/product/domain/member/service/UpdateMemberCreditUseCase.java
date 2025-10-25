package org.oneog.uppick.product.domain.member.service;

import org.oneog.uppick.product.domain.member.client.MemberClient;
import org.oneog.uppick.product.domain.member.dto.request.UpdateMemberCreditRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMemberCreditUseCase {

	private final MemberClient memberClient;

	public void execute(long memberId, long amount) {
		UpdateMemberCreditRequest updateMemberCreditRequest = new UpdateMemberCreditRequest(amount);

		memberClient.updateMemberCredit(memberId, updateMemberCreditRequest);
	}
}

package org.oneog.uppick.product.domain.member.service;

import org.oneog.uppick.product.domain.member.client.MemberClient;
import org.oneog.uppick.product.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterPurchaseDetailUseCase {

	private final MemberClient memberClient;

	public void execute(RegisterPurchaseDetailRequest request) {
		memberClient.registerPurchaseDetail(request);
	}
}

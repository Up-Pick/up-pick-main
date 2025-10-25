package org.oneog.uppick.product.domain.member.service;

import org.oneog.uppick.product.domain.member.client.MemberClient;
import org.oneog.uppick.product.domain.member.dto.request.RegisterSellDetailRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterSellDetailUseCase {

	private final MemberClient memberClient;

	public void execute(RegisterSellDetailRequest request) {
		memberClient.registerSellDetail(request);
	}
}

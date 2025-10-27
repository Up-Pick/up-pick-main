package org.oneog.uppick.product.domain.member.service;

import org.oneog.uppick.product.domain.member.client.MemberClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMemberCreditUseCase {
    private final MemberClient memberClient;

    public long execute(long memberId) {
        return memberClient.getMemberCredit(memberId);
    }
}

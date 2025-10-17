package org.oneog.uppick.domain.member.service;

import org.oneog.uppick.domain.member.entity.Member;

public interface MemberExternalServiceApi {
	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	void createUser(Member member);
}

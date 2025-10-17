package org.oneog.uppick.domain.member.service;

public interface MemberExternalServiceApi {
	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	void createUser(String email, String nickname, String encodedPassword);
}

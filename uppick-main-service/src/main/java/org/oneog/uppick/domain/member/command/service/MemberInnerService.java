package org.oneog.uppick.domain.member.command.service;

import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.member.command.entity.Member;

public interface MemberInnerService {

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	void createUser(SignupRequest request, String encodedPassword);

	Member findByEmail(String email);

}

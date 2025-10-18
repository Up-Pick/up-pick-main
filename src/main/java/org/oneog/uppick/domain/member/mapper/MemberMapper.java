package org.oneog.uppick.domain.member.mapper;

import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {
	public Member toEntity(SignupRequest request, String encodedPassword) {
		return Member.builder()
			.email(request.getEmail())
			.nickname(request.getNickname())
			.password(encodedPassword)
			.build();
	}
}

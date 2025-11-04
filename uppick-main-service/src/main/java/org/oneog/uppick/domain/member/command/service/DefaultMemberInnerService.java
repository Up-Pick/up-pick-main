package org.oneog.uppick.domain.member.command.service;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.auth.command.model.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.common.exception.AuthErrorCode;
import org.oneog.uppick.domain.member.command.entity.Member;
import org.oneog.uppick.domain.member.command.repository.MemberRepository;
import org.oneog.uppick.domain.member.common.mapper.MemberMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultMemberInnerService implements MemberInnerService {

	private final MemberRepository memberRepository;
	private final MemberMapper memberMapper;

	@Override
	public boolean existsByEmail(String email) {

		log.info("DefaultMemberInnerService - 이메일 존재 확인 시도");

		boolean exists = memberRepository.existsByEmail(email);

		log.info("DefaultMemberInnerService - 이메일 존재 확인 성공 ✅");

		return exists;
	}

	@Override
	public boolean existsByNickname(String nickname) {

		log.info("DefaultMemberInnerService - 닉네임 존재 확인 시도 ⏳");

		boolean exists = memberRepository.existsByNickname(nickname);

		log.info("DefaultMemberInnerService - 닉네임 존재 확인 성공 ✅");

		return exists;
	}

	@Override
	public void createUser(SignupRequest request, String encodedPassword) {

		log.info("DefaultMemberInnerService - 멤버 생성 시도 ⏳");

		Member member = memberMapper.toEntity(request, encodedPassword);
		memberRepository.save(member);

		log.info("DefaultMemberInnerService - 멤버 생성 성공 ✅");
	}

	@Override
	public Member findByEmail(String email) {

		log.info("DefaultMemberInnerService - 이메일로 멤버 조회 시도 ⏳");

		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.USER_NOT_FOUND));

		log.info("DefaultMemberInnerService - 이메일로 멤버 조회 성공 ✅");

		return member;
	}

}

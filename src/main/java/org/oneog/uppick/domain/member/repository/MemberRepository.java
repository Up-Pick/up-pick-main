package org.oneog.uppick.domain.member.repository;

import org.oneog.uppick.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public interface MemberRepository extends JpaRepository<Member, Long> {
	boolean existsByEmail(@Email(message = "유효한 이메일 형식이어야 합니다.") @NotBlank(message = "이메일은 필수입니다.") String email);

	boolean existsByNickname(
		@NotBlank(message = "닉네임은 필수입니다.") @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하입니다.") @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임에 특수문자를 사용할 수 없습니다.") String nickname);
}

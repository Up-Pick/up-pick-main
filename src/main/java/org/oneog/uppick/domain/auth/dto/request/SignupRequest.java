package org.oneog.uppick.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignupRequest {
	@Email(message = "유효한 이메일 형식이어야 합니다.")
	@NotBlank(message = "이메일은 필수입니다.")
	private String email;

	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하입니다.")
	@Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임에 특수문자를 사용할 수 없습니다.")
	private String nickname;

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,16}$",
		message = "비밀번호는 8~16자, 대문자 1개 이상, 소문자 1개 이상, 숫자 1개 이상, 특수문자 1개 이상을 포함해야 합니다."
	)
	private String password;
}

package org.oneog.uppick.domain.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.auth.dto.request.LoginRequest;
import org.oneog.uppick.domain.auth.dto.request.SignupRequest;
import org.oneog.uppick.domain.auth.dto.response.LoginResponse;
import org.oneog.uppick.domain.auth.service.AuthService;
import org.oneog.uppick.support.restdocs.RestDocsBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
class AuthControllerRestDocsTest extends RestDocsBase {

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthService authService;

	@Test
	@DisplayName("회원가입 API Rest Docs 문서화")
	void signup_정상요청_성공응답반환() throws Exception {

		// given
		// SignupRequest DTO 생성 (Builder 사용)
		SignupRequest request = SignupRequest.builder()
			.email("newuser@email.com")
			.nickname("새로운유저")
			.password("Password123!")
			.build();

		// authService.signup() 메소드는 void를 반환하므로 willDoNothing() 사용
		willDoNothing().given(authService).signup(any(SignupRequest.class));

		// when & then
		mockMvc.perform(
			post("/api/v1/members/signup") // 회원가입 엔드포인트
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk()) // 컨트롤러가 GlobalApiResponse.ok()를 반환하므로 200 OK
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
			.andDo(
				// 컨벤션: document("도메인-메서드명")
				document("auth-signup",
					requestFields( // 요청 본문 필드 정의
						fieldWithPath("email").type(JsonFieldType.STRING)
							.description("가입할 이메일 주소")
							.attributes(key("constraints").value("이메일 형식, NotBlank")),
						fieldWithPath("nickname").type(JsonFieldType.STRING)
							.description("사용할 닉네임")
							.attributes(key("constraints").value("2~10자, 특수문자 금지, NotBlank")),
						fieldWithPath("password").type(JsonFieldType.STRING)
							.description("사용할 비밀번호")
							.attributes(key("constraints").value("8~16자, 대/소문자, 숫자, 특수문자 각 1개 이상 포함, NotBlank"))),
					responseFields( // 응답 본문 필드 정의
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL).description("반환 데이터 (없음)") // Void 타입이므로 null
					)));
	}

	@Test
	@DisplayName("로그인 API Rest Docs 문서화")
	void login_정상요청_성공응답및토큰반환() throws Exception {

		// given
		LoginRequest request = new LoginRequest("test@email.com", "Password123!");
		LoginResponse response = new LoginResponse("Bearer eyJhbGciOiJIUzI1Ni...");

		given(authService.login(any(LoginRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(
			post("/api/v1/members/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.accessToken").value(response.getAccessToken()))
			.andDo(
				// 3. 컨벤션: document("도메인-메서드명")
				document("auth-login",
					requestFields(
						fieldWithPath("email").type(JsonFieldType.STRING)
							.description("로그인할 이메일")
							.attributes(key("constraints").value("이메일 형식, NotBlank")),
						fieldWithPath("password").type(JsonFieldType.STRING)
							.description("비밀번호")
							.attributes(key("constraints").value("NotBlank"))),
					responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
							.description("발급된 JWT 액세스 토큰 (Bearer 포함)"))));
	}

}
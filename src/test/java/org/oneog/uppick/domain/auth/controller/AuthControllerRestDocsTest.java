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
import org.oneog.uppick.domain.auth.dto.response.LoginResponse;
import org.oneog.uppick.domain.auth.service.AuthService;
import org.oneog.uppick.support.RestDocsBase;
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
	@DisplayName("로그인 API Rest Docs 문서화(성공)")
	void documentLogin() throws Exception {
		// given
		LoginRequest request = new LoginRequest("test@email.com", "Password123!");
		LoginResponse response = new LoginResponse("Bearer eyJhbGciOiJIUzI1Ni...");

		given(authService.login(any(LoginRequest.class))).willReturn(response);

		// when & then
		mockMvc.perform(
				post("/api/v1/members/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.accessToken").value(response.getAccessToken()))
			.andDo(
				// 3. 컨벤션: document("도메인-메서드명")
				document("auth-login",
					requestFields(
						fieldWithPath("email").type(JsonFieldType.STRING).description("로그인할 이메일")
							.attributes(key("constraints").value("이메일 형식, NotBlank")),
						fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
							.attributes(key("constraints").value("NotBlank"))
					),
					responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
							.description("발급된 JWT 액세스 토큰 (Bearer 포함)")
					)
				)
			);
	}
}
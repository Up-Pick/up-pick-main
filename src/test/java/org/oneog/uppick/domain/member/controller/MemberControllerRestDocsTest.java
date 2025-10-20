package org.oneog.uppick.domain.member.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.member.dto.request.CreditChargeRequest;
import org.oneog.uppick.domain.member.dto.response.CreditChargeResponse;
import org.oneog.uppick.domain.member.service.MemberInternalService;
import org.oneog.uppick.support.RestDocsBase;
import org.oneog.uppick.support.auth.WithMockAuthMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(MemberController.class)
class MemberControllerRestDocsTest extends RestDocsBase {

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private MemberInternalService memberInternalService;

	@Test
	@DisplayName("크레딧 충전 API Rest Docs 문서화")
	@WithMockAuthMember
	void documentCreditCharge() throws Exception {
		// given
		CreditChargeRequest request = new CreditChargeRequest(10000L);
		CreditChargeResponse response = new CreditChargeResponse(20000L); // 10000 충전 후 잔액 20000

		given(memberInternalService.chargeCredit(any(CreditChargeRequest.class), any()))
			.willReturn(response);

		// when & then
		mockMvc.perform(
				post("/api/v1/members/me/credit/charge")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))

			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
			.andExpect(jsonPath("$.data.currentCredit").value(response.getCurrentCredit()))
			.andDo(
				// 컨벤션: document("도메인-메서드명")
				document("member-chargeCredit",
					requestFields(
						fieldWithPath("amount").type(JsonFieldType.NUMBER).description("충전할 크레딧 금액")
							.attributes(key("constraints").value("0보다 커야 함 (양수), NotNull"))
					),
					responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("data.currentCredit").type(JsonFieldType.NUMBER).description("충전 후 현재 보유 크레딧")
					)
				)
			);
	}
}

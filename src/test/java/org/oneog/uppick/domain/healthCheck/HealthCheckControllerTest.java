package org.oneog.uppick.domain.healthCheck;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.healthCheck.Controller.HealthCheckController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import org.oneog.uppick.support.RestDocsConfig;

@WebMvcTest(
	controllers = HealthCheckController.class,
	excludeAutoConfiguration = SecurityAutoConfiguration.class  // Security 제외
)
@AutoConfigureRestDocs
@Import(RestDocsConfig.class)
public class HealthCheckControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("헬스체크 API 테스트")
	void healthCheck() throws Exception {
		mockMvc.perform(get("/health"))

			//테스트가 있어야 문서화 가능

			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
			.andExpect(jsonPath("$.data").value("UP"))

			//테스트 통과 후 문서화 과정

				.andDo(document("health-check"));
	}

	@Test
	@DisplayName("헬스체크 API 테스트 - 리스트")
	void healthCheck2() throws Exception {
		mockMvc.perform(get("/health2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.message").value("요청에 성공했습니다."))
			.andExpect(jsonPath("$.data").isArray())
			.andExpect(jsonPath("$.data[0]").value("서버 정상"))
			.andExpect(jsonPath("$.data[1]").value("DB 연결 OK"))
			.andExpect(jsonPath("$.data[2]").value("캐시 정상"))
				.andDo(document(
					"health-check-2",
					responseFields(
						fieldWithPath("success").description("성공 여부"),
						fieldWithPath("message").description("응답 메시지"),
						fieldWithPath("data[]").description("헬스체크 상태 목록")
					)
				));
	}

}

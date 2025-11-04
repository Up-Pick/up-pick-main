package org.oneog.uppick.domain.notification.controller;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.notification.command.controller.NotificationCommandController;
import org.oneog.uppick.domain.notification.command.entity.NotificationType;
import org.oneog.uppick.domain.notification.query.model.dto.response.GetUnreadNotificationsResponse;
import org.oneog.uppick.domain.notification.command.service.NotificationCommandService;
import org.oneog.uppick.support.auth.WithMockAuthMember;
import org.oneog.uppick.support.restdocs.RestDocsBase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(NotificationCommandController.class)
public class NotificationControllerRestDocsTest extends RestDocsBase {

	@MockitoBean
	private NotificationCommandService notificationCommandService;

	@Test
	@WithMockAuthMember
	void getUnreadNotifications() throws Exception {
		// Given
		List<GetUnreadNotificationsResponse.NotificationDetail> details = List.of(
			GetUnreadNotificationsResponse.NotificationDetail.builder()
				.type(NotificationType.BID)
				.title("입찰 성공")
				.message("입찰에 성공했습니다.")
				.notifiedAt(LocalDateTime.now())
				.build());
		GetUnreadNotificationsResponse response = new GetUnreadNotificationsResponse(details);
		when(notificationCommandService.getUnreadNotifications(1L)).thenReturn(response);

		// When & Then
		mockMvc.perform(get("/api/v1/notifications/me")
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isOk())
			.andDo(document("notifications-get-unread-notifications",
				requestHeaders(
					headerWithName("Authorization").description("JWT 인증 토큰 (Bearer 토큰)")),
				responseFields(
					fieldWithPath("success").description("API 요청 성공 여부").type(JsonFieldType.BOOLEAN),
					fieldWithPath("message").description("응답 메시지").type(JsonFieldType.STRING),
					fieldWithPath("data.notifications[].type").description("알림 타입 (예: BID)").type(JsonFieldType.STRING),
					fieldWithPath("data.notifications[].title").description("알림 제목").type(JsonFieldType.STRING),
					fieldWithPath("data.notifications[].message").description("알림 내용").type(JsonFieldType.STRING),
					fieldWithPath("data.notifications[].notifiedAt").description("알림 생성 시간 (ISO 8601 형식)")
						.type(JsonFieldType.STRING))));
	}
}

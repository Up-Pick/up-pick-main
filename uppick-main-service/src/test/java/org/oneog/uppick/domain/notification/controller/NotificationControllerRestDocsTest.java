package org.oneog.uppick.domain.notification.controller;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.notification.dto.response.GetUnreadNotificationsResponse;
import org.oneog.uppick.domain.notification.entity.NotificationType;
import org.oneog.uppick.domain.notification.service.NotificationInternalService;
import org.oneog.uppick.support.RestDocsBase;
import org.oneog.uppick.support.auth.WithMockAuthMember;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(NotificationController.class)
public class NotificationControllerRestDocsTest extends RestDocsBase {

    @MockitoBean
    private NotificationInternalService notificationInternalService;

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
        when(notificationInternalService.getUnreadNotifications(1L)).thenReturn(response);

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

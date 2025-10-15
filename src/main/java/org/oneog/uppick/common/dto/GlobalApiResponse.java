package org.oneog.uppick.common.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GlobalApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> GlobalApiResponse<T> ok(T data) {
        GlobalApiResponse<T> response = new GlobalApiResponse<>();
        response.success = true;
        response.message = "요청에 성공했습니다.";
        response.data = data;
        return response;
    }

    public static <T> GlobalApiResponse<T> ok(T data, String message) {
        GlobalApiResponse<T> response = new GlobalApiResponse<>();
        response.success = true;
        response.message = message;
        response.data = data;
        return response;
    }

    public static <T> GlobalApiResponse<T> fail(T data) {
        GlobalApiResponse<T> response = new GlobalApiResponse<>();
        response.success = false;
        response.message = "요청에 실패했습니다.";
        response.data = data;
        return response;
    }

    public static <T> GlobalApiResponse<T> fail(T data, String message) {
        GlobalApiResponse<T> response = new GlobalApiResponse<>();
        response.success = false;
        response.message = message;
        response.data = data;
        return response;
    }
}

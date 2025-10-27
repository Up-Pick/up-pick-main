package org.oneog.uppick.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GlobalApiResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;

    public static <T> GlobalApiResponse<T> ok(T data) {
        return new GlobalApiResponse<>(true, "요청에 성공했습니다.", data);
    }

    public static <T> GlobalApiResponse<T> ok(T data, String message) {
        return new GlobalApiResponse<>(true, message, data);
    }

    public static <T> GlobalApiResponse<T> fail(T data) {
        return new GlobalApiResponse<>(false, "요청에 실패했습니다.", data);
    }

    public static <T> GlobalApiResponse<T> fail(T data, String message) {
        return new GlobalApiResponse<>(false, message, data);
    }
}

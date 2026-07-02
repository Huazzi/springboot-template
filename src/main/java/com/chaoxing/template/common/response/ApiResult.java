package com.chaoxing.template.common.response;

import com.chaoxing.template.common.exception.ErrorCode;
import com.chaoxing.template.common.web.TraceIdHolder;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class ApiResult<T> {

  private final String code;
  private final String message;
  private final T data;
  private final String traceId;
  private final LocalDateTime timestamp;

  private ApiResult(String code, String message, T data, String traceId) {
    this.code = code;
    this.message = message;
    this.data = data;
    this.traceId = traceId;
    this.timestamp = LocalDateTime.now();
  }

  public static <T> ApiResult<T> success() {
    return success(null);
  }

  public static <T> ApiResult<T> success(T data) {
    return new ApiResult<>(
        ErrorCode.SUCCESS.getCode(),
        ErrorCode.SUCCESS.getMessage(),
        data,
        TraceIdHolder.getOrCreateTraceId());
  }

  public static <T> ApiResult<T> fail(ErrorCode errorCode) {
    ErrorCode resolvedErrorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    return fail(resolvedErrorCode, resolvedErrorCode.getMessage(), null);
  }

  public static <T> ApiResult<T> fail(ErrorCode errorCode, String message) {
    return fail(errorCode, message, null);
  }

  public static <T> ApiResult<T> fail(ErrorCode errorCode, String message, T data) {
    ErrorCode resolvedErrorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    String resolvedMessage =
        message == null || message.isBlank() ? errorCode.getMessage() : message;
    return new ApiResult<>(
        resolvedErrorCode.getCode(), resolvedMessage, data, TraceIdHolder.getOrCreateTraceId());
  }
}

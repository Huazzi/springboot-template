package com.chaoxing.template.common.response;

import com.chaoxing.template.common.exception.ErrorCode;
import com.chaoxing.template.common.web.TraceIdHolder;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class Result<T> {

  private static final String DATE_TIME_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";

  private final String code;
  private final String message;
  private final T data;
  private final String traceId;

  @JsonFormat(pattern = DATE_TIME_MILLIS)
  private final LocalDateTime timestamp;

  /** 统一通过工厂方法创建响应，确保 code、message、traceId 的生成规则一致。 */
  private Result(String code, String message, T data, String traceId) {
    this.code = code;
    this.message = message;
    this.data = data;
    this.traceId = traceId;
    this.timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
  }

  /** 构造无响应体的成功结果，常用于更新、删除等接口。 */
  public static <T> Result<T> success() {
    return success(null);
  }

  /** 构造带数据的成功结果，并附带当前请求 traceId，便于排查问题。 */
  public static <T> Result<T> success(T data) {
    return new Result<>(
        ErrorCode.SUCCESS.getCode(),
        ErrorCode.SUCCESS.getMessage(),
        data,
        TraceIdHolder.getOrCreateTraceId());
  }

  /** 使用错误码默认文案构造失败结果。 */
  public static <T> Result<T> fail(ErrorCode errorCode) {
    ErrorCode resolvedErrorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    return fail(resolvedErrorCode, resolvedErrorCode.getMessage(), null);
  }

  public static <T> Result<T> fail(ErrorCode errorCode, String message) {
    return fail(errorCode, message, null);
  }

  /** 构造带明细数据的失败结果，可用于字段校验错误或业务冲突详情。 */
  public static <T> Result<T> fail(ErrorCode errorCode, String message, T data) {
    ErrorCode resolvedErrorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
    String resolvedMessage =
        message == null || message.isBlank() ? errorCode.getMessage() : message;
    return new Result<>(
        resolvedErrorCode.getCode(), resolvedMessage, data, TraceIdHolder.getOrCreateTraceId());
  }
}

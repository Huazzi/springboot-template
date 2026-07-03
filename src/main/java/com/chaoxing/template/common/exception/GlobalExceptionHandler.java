package com.chaoxing.template.common.exception;

import com.chaoxing.template.common.response.Result;
import com.chaoxing.template.common.web.TraceIdHolder;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** 业务异常属于预期内失败，使用 warn 日志即可。 */
  @ExceptionHandler(ServiceException.class)
  public ResponseEntity<Result<Object>> handleServiceException(ServiceException exception) {
    log.warn(
        "Service exception, code={}, traceId={}, message={}",
        exception.getErrorCode().getCode(),
        TraceIdHolder.getOrCreateTraceId(),
        exception.getMessage());
    return buildResponse(exception.getErrorCode(), exception.getMessage(), exception.getData());
  }

  /** 处理 @RequestBody 参数上由 @Valid 触发的 JSON 请求体验证错误。 */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Result<Object>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception) {
    Map<String, String> errors = collectFieldErrors(exception.getBindingResult().getFieldErrors());
    return buildResponse(ErrorCode.PARAM_INVALID, ErrorCode.PARAM_INVALID.getMessage(), errors);
  }

  /** 处理 query/form 参数绑定时的校验错误，例如 pageNo、pageSize 约束。 */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<Result<Object>> handleBindException(BindException exception) {
    Map<String, String> errors = collectFieldErrors(exception.getBindingResult().getFieldErrors());
    return buildResponse(ErrorCode.PARAM_INVALID, ErrorCode.PARAM_INVALID.getMessage(), errors);
  }

  /** 处理方法级参数校验错误，例如路径变量上的 @Positive。 */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Result<Object>> handleConstraintViolation(
      ConstraintViolationException exception) {
    Map<String, String> errors = new LinkedHashMap<>();
    exception
        .getConstraintViolations()
        .forEach(
            violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
    return buildResponse(ErrorCode.PARAM_INVALID, ErrorCode.PARAM_INVALID.getMessage(), errors);
  }

  @ExceptionHandler({
    MissingServletRequestParameterException.class,
    TypeMismatchException.class,
    HttpMessageNotReadableException.class
  })
  public ResponseEntity<Result<Object>> handleBadRequest(Exception exception) {
    return buildResponse(ErrorCode.PARAM_INVALID, exception.getMessage(), null);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Result<Object>> handleMethodNotSupported(Exception exception) {
    return buildResponse(ErrorCode.METHOD_NOT_ALLOWED, exception.getMessage(), null);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Result<Object>> handleMediaTypeNotSupported(Exception exception) {
    return buildResponse(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED, exception.getMessage(), null);
  }

  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ResponseEntity<Result<Object>> handleNotFound(Exception exception) {
    return buildResponse(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage(), null);
  }

  /** 未预期异常记录完整堆栈，但返回给客户端的响应结构保持稳定。 */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Result<Object>> handleUnexpectedException(Exception exception) {
    log.error("Unexpected exception, traceId={}", TraceIdHolder.getOrCreateTraceId(), exception);
    return buildResponse(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), null);
  }

  /** 保持字段错误顺序稳定，方便阅读和测试断言。 */
  private Map<String, String> collectFieldErrors(Iterable<FieldError> fieldErrors) {
    Map<String, String> errors = new LinkedHashMap<>();
    fieldErrors.forEach(
        fieldError ->
            errors.put(fieldError.getField(), resolveMessage(fieldError.getDefaultMessage())));
    return errors;
  }

  private ResponseEntity<Result<Object>> buildResponse(
      ErrorCode errorCode, String message, Object data) {
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(Result.fail(errorCode, message, data));
  }

  private String resolveMessage(String message) {
    return message == null || message.isBlank() ? ErrorCode.PARAM_INVALID.getMessage() : message;
  }
}

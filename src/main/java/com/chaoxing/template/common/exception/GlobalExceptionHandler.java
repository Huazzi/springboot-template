package com.chaoxing.template.common.exception;

import com.chaoxing.template.common.response.ApiResult;
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

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResult<Object>> handleBusinessException(BusinessException exception) {
    log.warn(
        "Business exception, code={}, traceId={}, message={}",
        exception.getErrorCode().getCode(),
        TraceIdHolder.getOrCreateTraceId(),
        exception.getMessage());
    return buildResponse(exception.getErrorCode(), exception.getMessage(), exception.getData());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResult<Object>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception) {
    Map<String, String> errors = collectFieldErrors(exception.getBindingResult().getFieldErrors());
    return buildResponse(ErrorCode.PARAM_INVALID, ErrorCode.PARAM_INVALID.getMessage(), errors);
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResult<Object>> handleBindException(BindException exception) {
    Map<String, String> errors = collectFieldErrors(exception.getBindingResult().getFieldErrors());
    return buildResponse(ErrorCode.PARAM_INVALID, ErrorCode.PARAM_INVALID.getMessage(), errors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResult<Object>> handleConstraintViolation(
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
  public ResponseEntity<ApiResult<Object>> handleBadRequest(Exception exception) {
    return buildResponse(ErrorCode.PARAM_INVALID, exception.getMessage(), null);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResult<Object>> handleMethodNotSupported(Exception exception) {
    return buildResponse(ErrorCode.METHOD_NOT_ALLOWED, exception.getMessage(), null);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ApiResult<Object>> handleMediaTypeNotSupported(Exception exception) {
    return buildResponse(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED, exception.getMessage(), null);
  }

  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ResponseEntity<ApiResult<Object>> handleNotFound(Exception exception) {
    return buildResponse(ErrorCode.NOT_FOUND, ErrorCode.NOT_FOUND.getMessage(), null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResult<Object>> handleUnexpectedException(Exception exception) {
    log.error("Unexpected exception, traceId={}", TraceIdHolder.getOrCreateTraceId(), exception);
    return buildResponse(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage(), null);
  }

  private Map<String, String> collectFieldErrors(Iterable<FieldError> fieldErrors) {
    Map<String, String> errors = new LinkedHashMap<>();
    fieldErrors.forEach(
        fieldError ->
            errors.put(fieldError.getField(), resolveMessage(fieldError.getDefaultMessage())));
    return errors;
  }

  private ResponseEntity<ApiResult<Object>> buildResponse(
      ErrorCode errorCode, String message, Object data) {
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(ApiResult.fail(errorCode, message, data));
  }

  private String resolveMessage(String message) {
    return message == null || message.isBlank() ? ErrorCode.PARAM_INVALID.getMessage() : message;
  }
}

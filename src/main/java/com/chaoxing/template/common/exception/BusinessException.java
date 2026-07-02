package com.chaoxing.template.common.exception;

import java.io.Serial;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final ErrorCode errorCode;
  private final Object data;

  public BusinessException(String message) {
    this(ErrorCode.BUSINESS_ERROR, message, null);
  }

  public BusinessException(ErrorCode errorCode) {
    this(errorCode, errorCode.getMessage(), null);
  }

  public BusinessException(ErrorCode errorCode, String message) {
    this(errorCode, message, null);
  }

  public BusinessException(ErrorCode errorCode, String message, Object data) {
    super(message);
    this.errorCode = errorCode == null ? ErrorCode.BUSINESS_ERROR : errorCode;
    this.data = data;
  }
}

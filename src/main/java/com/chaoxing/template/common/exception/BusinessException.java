package com.chaoxing.template.common.exception;

import java.io.Serial;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  private final ErrorCode errorCode;
  private final Object data;

  /** 默认业务异常，适用于只需要返回可读错误信息的场景。 */
  public BusinessException(String message) {
    this(ErrorCode.BUSINESS_ERROR, message, null);
  }

  public BusinessException(ErrorCode errorCode) {
    this(errorCode, errorCode.getMessage(), null);
  }

  public BusinessException(ErrorCode errorCode, String message) {
    this(errorCode, message, null);
  }

  /** data 可携带安全的业务明细，例如校验提示或冲突字段。 */
  public BusinessException(ErrorCode errorCode, String message, Object data) {
    super(message);
    this.errorCode = errorCode == null ? ErrorCode.BUSINESS_ERROR : errorCode;
    this.data = data;
  }
}

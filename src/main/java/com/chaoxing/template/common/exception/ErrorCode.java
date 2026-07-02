package com.chaoxing.template.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  SUCCESS("00000", "成功", HttpStatus.OK),
  PARAM_INVALID("A0400", "请求参数错误", HttpStatus.BAD_REQUEST),
  UNAUTHORIZED("A0401", "未认证", HttpStatus.UNAUTHORIZED),
  FORBIDDEN("A0403", "无权限访问", HttpStatus.FORBIDDEN),
  NOT_FOUND("A0404", "资源不存在", HttpStatus.NOT_FOUND),
  METHOD_NOT_ALLOWED("A0405", "请求方法不支持", HttpStatus.METHOD_NOT_ALLOWED),
  MEDIA_TYPE_NOT_SUPPORTED("A0415", "请求媒体类型不支持", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
  BUSINESS_ERROR("B0001", "业务处理失败", HttpStatus.BAD_REQUEST),
  SYSTEM_ERROR("B0500", "系统异常", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}

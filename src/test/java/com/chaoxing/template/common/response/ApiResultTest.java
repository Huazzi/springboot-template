package com.chaoxing.template.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.chaoxing.template.common.exception.ErrorCode;
import com.chaoxing.template.common.web.TraceIdHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ApiResultTest {

  @AfterEach
  void tearDown() {
    TraceIdHolder.clear();
  }

  @Test
  void shouldBuildSuccessResultWithTraceId() {
    TraceIdHolder.setTraceId("trace-001");

    ApiResult<String> result = ApiResult.success("ok");

    assertThat(result.getCode()).isEqualTo(ErrorCode.SUCCESS.getCode());
    assertThat(result.getMessage()).isEqualTo(ErrorCode.SUCCESS.getMessage());
    assertThat(result.getData()).isEqualTo("ok");
    assertThat(result.getTraceId()).isEqualTo("trace-001");
    assertThat(result.getTimestamp()).isNotNull();
  }

  @Test
  void shouldBuildFailureResultWithCustomMessage() {
    ApiResult<Object> result = ApiResult.fail(ErrorCode.PARAM_INVALID, "name must not be blank");

    assertThat(result.getCode()).isEqualTo(ErrorCode.PARAM_INVALID.getCode());
    assertThat(result.getMessage()).isEqualTo("name must not be blank");
    assertThat(result.getTraceId()).isNotBlank();
  }
}

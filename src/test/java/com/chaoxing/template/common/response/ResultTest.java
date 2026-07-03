package com.chaoxing.template.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.chaoxing.template.common.exception.ErrorCode;
import com.chaoxing.template.common.web.TraceIdHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ResultTest {

  @AfterEach
  void tearDown() {
    TraceIdHolder.clear();
  }

  @Test
  void shouldBuildSuccessResultWithTraceId() {
    TraceIdHolder.setTraceId("trace-001");

    Result<String> result = Result.success("ok");

    assertThat(result.getCode()).isEqualTo(ErrorCode.SUCCESS.getCode());
    assertThat(result.getMessage()).isEqualTo(ErrorCode.SUCCESS.getMessage());
    assertThat(result.getData()).isEqualTo("ok");
    assertThat(result.getTraceId()).isEqualTo("trace-001");
    assertThat(result.getTimestamp()).isNotNull();
    assertThat(result.getTimestamp().getNano()).isLessThan(1_000_000_000);
    assertThat(result.getTimestamp().getNano() % 1_000_000).isZero();
  }

  @Test
  void shouldBuildFailureResultWithCustomMessage() {
    Result<Object> result = Result.fail(ErrorCode.PARAM_INVALID, "name must not be blank");

    assertThat(result.getCode()).isEqualTo(ErrorCode.PARAM_INVALID.getCode());
    assertThat(result.getMessage()).isEqualTo("name must not be blank");
    assertThat(result.getTraceId()).isNotBlank();
  }
}

package com.chaoxing.template.common.web;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

public final class TraceIdHolder {

  public static final String TRACE_ID = "traceId";

  private TraceIdHolder() {}

  public static String getTraceId() {
    return MDC.get(TRACE_ID);
  }

  public static String getOrCreateTraceId() {
    String traceId = getTraceId();
    if (StringUtils.hasText(traceId)) {
      return traceId;
    }
    String generatedTraceId = UUID.randomUUID().toString().replace("-", "");
    setTraceId(generatedTraceId);
    return generatedTraceId;
  }

  public static void setTraceId(String traceId) {
    if (StringUtils.hasText(traceId)) {
      MDC.put(TRACE_ID, traceId.trim());
    }
  }

  public static void clear() {
    MDC.remove(TRACE_ID);
  }
}

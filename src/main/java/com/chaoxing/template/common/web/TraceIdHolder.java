package com.chaoxing.template.common.web;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

/**
 * TraceIdHolder 用于在当前线程中存储和获取 traceId，方便日志追踪。
 * <p>
 * traceId 主要用于日志追踪，方便在分布式系统中定位问题。
 * <p>
 * 它通常在请求开始时生成，并在请求结束时清理。
 */
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

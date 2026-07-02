package com.chaoxing.template.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

  public static final String HEADER_TRACE_ID = "X-Trace-Id";

  /** 为一次请求绑定一个 traceId，并在请求结束后清理线程上下文。 */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String traceId = resolveTraceId(request);
    TraceIdHolder.setTraceId(traceId);
    response.setHeader(HEADER_TRACE_ID, traceId);
    try {
      filterChain.doFilter(request, response);
    } finally {
      TraceIdHolder.clear();
    }
  }

  /** 优先使用调用方传入的 traceId，方便和上游系统日志串联。 */
  private String resolveTraceId(HttpServletRequest request) {
    String traceId = request.getHeader(HEADER_TRACE_ID);
    if (StringUtils.hasText(traceId)) {
      return traceId.trim();
    }
    return UUID.randomUUID().toString().replace("-", "");
  }
}

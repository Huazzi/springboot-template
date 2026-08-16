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

/**
 * 日志追踪过滤器
 * 为每一次请求生成一个 traceId，并在请求结束后清理线程上下文。
 * <p>
 * traceId 主要用于日志追踪，方便在分布式系统中定位问题。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

  public static final String HEADER_TRACE_ID = "X-Trace-Id";

  /**
   * 为一次请求绑定一个 traceId，并在请求结束后清理线程上下文。
   *
   * @param request  HTTP 请求对象
   * @param response HTTP 响应对象
   * @param filterChain 过滤器链
   * @throws ServletException 如果请求处理过程中发生异常
   * @throws IOException 如果请求处理过程中发生 I/O 异常
   */
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

  /**
   * 优先使用调用方传入的 traceId，方便和上游系统日志串联。
   *
   * @param request HTTP 请求对象
   * @return traceId
   */
  private String resolveTraceId(HttpServletRequest request) {
    String traceId = request.getHeader(HEADER_TRACE_ID);
    if (StringUtils.hasText(traceId)) {
      return traceId.trim();
    }
    return UUID.randomUUID().toString().replace("-", "");
  }
}

package com.chaoxing.template.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chaoxing.template.common.web.TraceIdFilter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  private static final String TRACE_ID = "trace-test";

  private MockMvc mockMvc;
  private LocalValidatorFactoryBean validator;

  @BeforeEach
  void setUp() {
    validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new TraceIdFilter())
            .setValidator(validator)
            .build();
  }

  @AfterEach
  void tearDown() {
    validator.close();
  }

  @Test
  void shouldHandleBusinessExceptionAndKeepTraceId() throws Exception {
    mockMvc
        .perform(get("/test/business").header(TraceIdFilter.HEADER_TRACE_ID, TRACE_ID))
        .andExpect(status().isBadRequest())
        .andExpect(header().string(TraceIdFilter.HEADER_TRACE_ID, TRACE_ID))
        .andExpect(jsonPath("$.code").value(ErrorCode.BUSINESS_ERROR.getCode()))
        .andExpect(jsonPath("$.message").value("user name exists"))
        .andExpect(jsonPath("$.traceId").value(TRACE_ID));
  }

  @Test
  void shouldHandleValidationException() throws Exception {
    mockMvc
        .perform(post("/test/validated").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_INVALID.getCode()))
        .andExpect(jsonPath("$.data.name").value("姓名不能为空"))
        .andExpect(jsonPath("$.traceId").isNotEmpty());
  }

  @RestController
  @RequestMapping("/test")
  static class TestController {

    @GetMapping("/business")
    public void business() {
      throw new BusinessException("user name exists");
    }

    @PostMapping("/validated")
    public void validated(@Valid @RequestBody TestRequest request) {}
  }

  record TestRequest(@NotBlank(message = "姓名不能为空") String name) {}
}

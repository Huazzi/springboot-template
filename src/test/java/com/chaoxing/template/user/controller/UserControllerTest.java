package com.chaoxing.template.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chaoxing.template.common.exception.ErrorCode;
import com.chaoxing.template.common.exception.GlobalExceptionHandler;
import com.chaoxing.template.common.response.PageResult;
import com.chaoxing.template.common.web.TraceIdFilter;
import com.chaoxing.template.user.request.UserCreateRequest;
import com.chaoxing.template.user.request.UserQueryRequest;
import com.chaoxing.template.user.request.UserUpdateRequest;
import com.chaoxing.template.user.response.UserResponse;
import com.chaoxing.template.user.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class UserControllerTest {

  private static final String TRACE_ID = "trace-user-test";

  private UserService userService;
  private MockMvc mockMvc;
  private LocalValidatorFactoryBean validator;

  @BeforeEach
  void setUp() {
    userService = mock(UserService.class);
    validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(new UserController(userService))
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
  void shouldCreateUser() throws Exception {
    when(userService.create(any(UserCreateRequest.class))).thenReturn(userResponse());

    mockMvc
        .perform(
            post("/api/v1/users")
                .header(TraceIdFilter.HEADER_TRACE_ID, TRACE_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"username":"alice","nickname":"Alice","email":"alice@example.com"}
                    """))
        .andExpect(status().isOk())
        .andExpect(header().string(TraceIdFilter.HEADER_TRACE_ID, TRACE_ID))
        .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.username").value("alice"))
        .andExpect(jsonPath("$.data.createdAt").value("2026-07-02 10:00:00.000"));

    ArgumentCaptor<UserCreateRequest> captor = ArgumentCaptor.forClass(UserCreateRequest.class);
    verify(userService).create(captor.capture());
    assertThat(captor.getValue().getUsername()).isEqualTo("alice");
  }

  @Test
  void shouldRejectInvalidCreateRequest() throws Exception {
    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(ErrorCode.PARAM_INVALID.getCode()))
        .andExpect(jsonPath("$.data.username").value("用户名不能为空"))
        .andExpect(jsonPath("$.data.nickname").value("昵称不能为空"));
  }

  @Test
  void shouldGetUserById() throws Exception {
    when(userService.getById(1L)).thenReturn(userResponse());

    mockMvc
        .perform(get("/api/v1/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id").value(1))
        .andExpect(jsonPath("$.data.nickname").value("Alice"));
  }

  @Test
  void shouldQueryUserPage() throws Exception {
    PageResult<UserResponse> page = PageResult.of(List.of(userResponse()), 1, 1, 10);
    when(userService.page(any(UserQueryRequest.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/v1/users?pageNo=1&pageSize=10&username=alice"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.total").value(1))
        .andExpect(jsonPath("$.data.records[0].username").value("alice"));
  }

  @Test
  void shouldUpdateUser() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nickname\":\"New Alice\",\"status\":1}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()));

    verify(userService).update(any(Long.class), any(UserUpdateRequest.class));
  }

  @Test
  void shouldDeleteUser() throws Exception {
    mockMvc
        .perform(delete("/api/v1/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()));

    verify(userService).delete(1L);
  }

  private UserResponse userResponse() {
    UserResponse response = new UserResponse();
    response.setId(1L);
    response.setUsername("alice");
    response.setNickname("Alice");
    response.setEmail("alice@example.com");
    response.setMobile("13800000000");
    response.setStatus(1);
    response.setCreatedAt(LocalDateTime.of(2026, 7, 2, 10, 0));
    response.setUpdatedAt(LocalDateTime.of(2026, 7, 2, 10, 0));
    return response;
  }
}

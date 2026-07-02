package com.chaoxing.template.user.controller;

import com.chaoxing.template.common.response.ApiResult;
import com.chaoxing.template.common.response.PageResult;
import com.chaoxing.template.user.request.UserCreateRequest;
import com.chaoxing.template.user.request.UserQueryRequest;
import com.chaoxing.template.user.request.UserUpdateRequest;
import com.chaoxing.template.user.response.UserResponse;
import com.chaoxing.template.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;

  @PostMapping
  public ApiResult<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
    return ApiResult.success(userService.create(request));
  }

  @GetMapping("/{id}")
  public ApiResult<UserResponse> getById(@Positive(message = "用户ID必须为正整数") @PathVariable Long id) {
    return ApiResult.success(userService.getById(id));
  }

  @GetMapping
  public ApiResult<PageResult<UserResponse>> page(@Valid UserQueryRequest request) {
    return ApiResult.success(userService.page(request));
  }

  @PutMapping("/{id}")
  public ApiResult<Void> update(
      @Positive(message = "用户ID必须为正整数") @PathVariable Long id,
      @Valid @RequestBody UserUpdateRequest request) {
    userService.update(id, request);
    return ApiResult.success();
  }

  @DeleteMapping("/{id}")
  public ApiResult<Void> delete(@Positive(message = "用户ID必须为正整数") @PathVariable Long id) {
    userService.delete(id);
    return ApiResult.success();
  }
}

package com.chaoxing.template.user.controller;

import com.chaoxing.template.common.response.PageResult;
import com.chaoxing.template.common.response.Result;
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

  /** 创建后返回新用户信息，方便调用方立即拿到数据库生成的 ID。 */
  @PostMapping
  public Result<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
    return Result.success(userService.create(request));
  }

  @GetMapping("/{id}")
  public Result<UserResponse> getById(@Positive(message = "用户ID必须为正整数") @PathVariable Long id) {
    return Result.success(userService.getById(id));
  }

  /** 查询参数绑定到 UserQueryRequest，并在进入 Service 前完成校验。 */
  @GetMapping
  public Result<PageResult<UserResponse>> page(@Valid UserQueryRequest request) {
    return Result.success(userService.page(request));
  }

  @PutMapping("/{id}")
  public Result<Void> update(
      @Positive(message = "用户ID必须为正整数") @PathVariable Long id,
      @Valid @RequestBody UserUpdateRequest request) {
    userService.update(id, request);
    return Result.success();
  }

  /** 删除在 Service/Mapper 层实现为逻辑删除。 */
  @DeleteMapping("/{id}")
  public Result<Void> delete(@Positive(message = "用户ID必须为正整数") @PathVariable Long id) {
    userService.delete(id);
    return Result.success();
  }
}

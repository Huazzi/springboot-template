package com.chaoxing.template.user.service;

import com.chaoxing.template.common.response.PageResult;
import com.chaoxing.template.user.request.UserCreateRequest;
import com.chaoxing.template.user.request.UserQueryRequest;
import com.chaoxing.template.user.request.UserUpdateRequest;
import com.chaoxing.template.user.response.UserResponse;

public interface UserService {

  UserResponse create(UserCreateRequest request);

  UserResponse getById(Long id);

  PageResult<UserResponse> page(UserQueryRequest request);

  void update(Long id, UserUpdateRequest request);

  void delete(Long id);
}

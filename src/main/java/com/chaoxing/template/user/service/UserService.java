package com.chaoxing.template.user.service;

import com.chaoxing.template.common.response.PageResult;
import com.chaoxing.template.user.request.UserCreateRequest;
import com.chaoxing.template.user.request.UserLoadMoreRequest;
import com.chaoxing.template.user.request.UserQueryRequest;
import com.chaoxing.template.user.request.UserUpdateRequest;
import com.chaoxing.template.user.response.UserResponse;
import java.util.List;

public interface UserService {

  UserResponse create(UserCreateRequest request);

  UserResponse getById(Long id);

  PageResult<UserResponse> page(UserQueryRequest request);

  /** 加载更多：keyset 游标分页，lastId 为空表示第一页。 */
  List<UserResponse> loadMore(UserLoadMoreRequest request);

  void update(Long id, UserUpdateRequest request);

  void delete(Long id);
}

package com.chaoxing.template.user.service.impl;

import com.chaoxing.template.common.exception.ErrorCode;
import com.chaoxing.template.common.exception.ServiceException;
import com.chaoxing.template.common.response.PageResult;
import com.chaoxing.template.user.entity.UserEntity;
import com.chaoxing.template.user.mapper.UserMapper;
import com.chaoxing.template.user.request.UserCreateRequest;
import com.chaoxing.template.user.request.UserQueryRequest;
import com.chaoxing.template.user.request.UserUpdateRequest;
import com.chaoxing.template.user.response.UserResponse;
import com.chaoxing.template.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final int DEFAULT_ENABLED_STATUS = 1;

  private final UserMapper userMapper;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public UserResponse create(UserCreateRequest request) {
    String username = trimToNull(request.getUsername());
    if (userMapper.countByUsername(username) > 0) {
      throw new ServiceException("用户名已存在");
    }

    UserEntity entity = new UserEntity();
    entity.setUsername(username);
    entity.setNickname(trimToNull(request.getNickname()));
    entity.setEmail(trimToNull(request.getEmail()));
    entity.setMobile(trimToNull(request.getMobile()));
    entity.setStatus(request.getStatus() == null ? DEFAULT_ENABLED_STATUS : request.getStatus());

    try {
      userMapper.insert(entity);
    } catch (DuplicateKeyException exception) {
      // 唯一索引用于兜底处理多个并发请求同时通过前置检查的情况。
      throw new ServiceException("用户名已存在");
    }
    return getById(entity.getId());
  }

  @Override
  public UserResponse getById(Long id) {
    return UserResponse.from(getExistingUser(id));
  }

  @Override
  public PageResult<UserResponse> page(UserQueryRequest request) {
    long pageNo = Math.max(request.getPageNo(), 1);
    long pageSize = Math.min(Math.max(request.getPageSize(), 1), 100);
    long total = userMapper.countByCondition(request);
    if (total == 0) {
      // 即使没有匹配记录，也返回规范化后的分页信息。
      return PageResult.empty(pageNo, pageSize);
    }

    long offset = (pageNo - 1) * pageSize;
    List<UserResponse> records =
        userMapper.selectPage(request, offset, pageSize).stream().map(UserResponse::from).toList();
    return PageResult.of(records, total, pageNo, pageSize);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void update(Long id, UserUpdateRequest request) {
    UserEntity entity = new UserEntity();
    entity.setId(id);
    entity.setNickname(trimToNull(request.getNickname()));
    entity.setEmail(trimToNull(request.getEmail()));
    entity.setMobile(trimToNull(request.getMobile()));
    entity.setStatus(request.getStatus());

    if (!hasUpdateContent(entity)) {
      // 避免执行只刷新 updated_at、没有任何业务字段变化的 UPDATE。
      throw new ServiceException(ErrorCode.PARAM_INVALID, "至少提供一个待更新字段");
    }

    int updated = userMapper.updateById(entity);
    if (updated == 0) {
      throw userNotFoundException();
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id) {
    int deleted = userMapper.logicDeleteById(id);
    if (deleted == 0) {
      throw userNotFoundException();
    }
  }

  private UserEntity getExistingUser(Long id) {
    UserEntity entity = userMapper.selectById(id);
    if (entity == null) {
      throw userNotFoundException();
    }
    return entity;
  }

  private boolean hasUpdateContent(UserEntity entity) {
    return entity.getNickname() != null
        || entity.getEmail() != null
        || entity.getMobile() != null
        || entity.getStatus() != null;
  }

  private ServiceException userNotFoundException() {
    return new ServiceException(ErrorCode.NOT_FOUND, "用户不存在");
  }

  private String trimToNull(String value) {
    if (!StringUtils.hasText(value)) {
      return null;
    }
    // 统一清洗空白输入，让 SQL 条件和更新判断保持简单。
    return value.trim();
  }
}

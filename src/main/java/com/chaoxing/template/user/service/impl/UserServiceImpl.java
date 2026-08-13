package com.chaoxing.template.user.service.impl;

import com.chaoxing.template.common.exception.ErrorCode;
import com.chaoxing.template.common.exception.ServiceException;
import com.chaoxing.template.common.response.PageResult;
import com.chaoxing.template.user.entity.UserEntity;
import com.chaoxing.template.user.mapper.UserMapper;
import com.chaoxing.template.user.request.UserCreateRequest;
import com.chaoxing.template.user.request.UserLoadMoreRequest;
import com.chaoxing.template.user.request.UserQueryRequest;
import com.chaoxing.template.user.request.UserUpdateRequest;
import com.chaoxing.template.user.response.UserResponse;
import com.chaoxing.template.user.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final int DEFAULT_ENABLED_STATUS = 1;

  /** 用户详情 Redis value 的 key 前缀 */
  private static final String USER_DETAIL_KEY_PREFIX = "user:detail:";

  /** 过期时间为 30 分钟 */
  private static final Duration USER_CACHE_TTL = Duration.ofMinutes(30);

  private final UserMapper userMapper;

  private final StringRedisTemplate stringRedisTemplate;

  private final ObjectMapper objectMapper;

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

  /**
   * 根据 id 查询用户信息
   *
   * @param id 用户ID
   * @return 用户信息响应体
   */
  @Override
  public UserResponse getById(Long id) {
    String key = USER_DETAIL_KEY_PREFIX + id;

    // 1. 先查缓存
    String cachedUserDetail = stringRedisTemplate.opsForValue().get(key);
    if (StringUtils.hasText(cachedUserDetail)) {
      try {
        return objectMapper.readValue(cachedUserDetail, UserResponse.class);
      } catch (JsonProcessingException e) {
        stringRedisTemplate.delete(key);
      }
    }

    // 2.  缓存未命中，查数据库
    UserResponse response = UserResponse.from(getExistingUser(id));

    // 3. 回填缓存，并设置 30 分钟过期
    putCache(key, response);

    return response;
  }

  /**
   * 分页查询
   *
   * @param request 用户请求实体
   * @return 结果响应体
   */
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

  /** 加载更多：不做 count 查询，直接按游标取一页数据。 是否还有更多由调用方判断：返回条数 &lt; pageSize 即表示已到末尾。 */
  @Override
  public List<UserResponse> loadMore(UserLoadMoreRequest request) {
    // 1 <= pageSize <= 100
    int pageSize = (int) Math.min(Math.max(request.getPageSize(), 1), 100);
    return userMapper.selectLoadMore(request, pageSize).stream().map(UserResponse::from).toList();
  }

  /**
   * 更新用户信息
   *
   * @param id 用户ID
   * @param request 更新请求体
   */
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

    // 删除过时的 Redis 缓存
    stringRedisTemplate.delete(USER_DETAIL_KEY_PREFIX + id);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id) {
    int deleted = userMapper.logicDeleteById(id);
    if (deleted == 0) {
      throw userNotFoundException();
    }

    // 删除过时的 Redis 缓存
    stringRedisTemplate.delete(USER_DETAIL_KEY_PREFIX + id);
  }

  private UserEntity getExistingUser(Long id) {
    UserEntity entity = userMapper.selectById(id);
    if (entity == null) {
      throw userNotFoundException();
    }
    return entity;
  }

  /**
   * 回填缓存：如果在事务中，推迟到事务提交之后再写入，避免回滚后留下脏数据
   *
   * @param key redis键
   * @param response 响应体
   */
  private void putCache(String key, UserResponse response) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      // 在事务中，在提交之后再回写缓存
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              writeCache(key, response);
            }
          });
    } else {
      // 不在事务中，直接回写缓存
      writeCache(key, response);
    }
  }

  /** 回填缓存 */
  private void writeCache(String key, UserResponse response) {
    try {
      stringRedisTemplate
          .opsForValue()
          .set(key, objectMapper.writeValueAsString(response), USER_CACHE_TTL);
    } catch (JsonProcessingException e) {
      // 序列化失败不阻塞主流程，本次不写入即可
    }
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

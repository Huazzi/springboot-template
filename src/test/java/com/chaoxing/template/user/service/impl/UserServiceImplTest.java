package com.chaoxing.template.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chaoxing.template.common.exception.BusinessException;
import com.chaoxing.template.common.exception.ErrorCode;
import com.chaoxing.template.common.response.PageResult;
import com.chaoxing.template.user.entity.UserEntity;
import com.chaoxing.template.user.mapper.UserMapper;
import com.chaoxing.template.user.request.UserCreateRequest;
import com.chaoxing.template.user.request.UserQueryRequest;
import com.chaoxing.template.user.request.UserUpdateRequest;
import com.chaoxing.template.user.response.UserResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UserMapper userMapper;

  @InjectMocks private UserServiceImpl userService;

  @Test
  void shouldCreateUserAndReturnResponse() {
    UserCreateRequest request = new UserCreateRequest();
    request.setUsername(" alice ");
    request.setNickname(" Alice ");

    when(userMapper.countByUsername("alice")).thenReturn(0);
    when(userMapper.insert(any(UserEntity.class)))
        .thenAnswer(
            invocation -> {
              UserEntity entity = invocation.getArgument(0);
              entity.setId(1L);
              return 1;
            });
    when(userMapper.selectById(1L)).thenReturn(activeUser());

    UserResponse response = userService.create(request);

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userMapper).insert(captor.capture());
    assertThat(captor.getValue().getUsername()).isEqualTo("alice");
    assertThat(captor.getValue().getNickname()).isEqualTo("Alice");
    assertThat(captor.getValue().getStatus()).isEqualTo(1);
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getUsername()).isEqualTo("alice");
  }

  @Test
  void shouldRejectDuplicateUsername() {
    UserCreateRequest request = new UserCreateRequest();
    request.setUsername("alice");

    when(userMapper.countByUsername("alice")).thenReturn(1);

    assertThatThrownBy(() -> userService.create(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("用户名已存在");
    verify(userMapper, never()).insert(any());
  }

  @Test
  void shouldQueryPagedUsers() {
    UserQueryRequest request = new UserQueryRequest();
    request.setPageNo(2);
    request.setPageSize(5);

    when(userMapper.countByCondition(request)).thenReturn(6L);
    when(userMapper.selectPage(request, 5, 5)).thenReturn(List.of(activeUser()));

    PageResult<UserResponse> page = userService.page(request);

    assertThat(page.getTotal()).isEqualTo(6);
    assertThat(page.getPageNo()).isEqualTo(2);
    assertThat(page.getPageSize()).isEqualTo(5);
    assertThat(page.getPages()).isEqualTo(2);
    assertThat(page.getRecords()).hasSize(1);
  }

  @Test
  void shouldRejectEmptyUpdateContent() {
    UserUpdateRequest request = new UserUpdateRequest();

    assertThatThrownBy(() -> userService.update(1L, request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("至少提供一个待更新字段");
    verify(userMapper, never()).updateById(any());
  }

  @Test
  void shouldThrowNotFoundWhenDeletingMissingUser() {
    when(userMapper.logicDeleteById(1L)).thenReturn(0);

    assertThatThrownBy(() -> userService.delete(1L))
        .isInstanceOfSatisfying(
            BusinessException.class,
            exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
  }

  private UserEntity activeUser() {
    UserEntity entity = new UserEntity();
    entity.setId(1L);
    entity.setUsername("alice");
    entity.setNickname("Alice");
    entity.setEmail("alice@example.com");
    entity.setMobile("13800000000");
    entity.setStatus(1);
    entity.setDeleted(false);
    entity.setCreatedAt(LocalDateTime.of(2026, 7, 2, 10, 0));
    entity.setUpdatedAt(LocalDateTime.of(2026, 7, 2, 10, 0));
    return entity;
  }
}

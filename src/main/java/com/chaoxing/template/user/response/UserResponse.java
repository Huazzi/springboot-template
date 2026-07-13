package com.chaoxing.template.user.response;

import com.chaoxing.template.user.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

  private static final String DATE_TIME_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";

  private Long id;
  private String username;
  private String nickname;
  private String email;
  private String mobile;
  private Integer status;

  @JsonFormat(pattern = DATE_TIME_MILLIS)
  private LocalDateTime createdAt;

  @JsonFormat(pattern = DATE_TIME_MILLIS)
  private LocalDateTime updatedAt;

  /**
   * 将数据库实体转换为响应对象
   *
   * @param entity 数据库实体
   * @return 响应对象
   */
  public static UserResponse from(UserEntity entity) {
    UserResponse response = new UserResponse();
    response.setId(entity.getId());
    response.setUsername(entity.getUsername());
    response.setNickname(entity.getNickname());
    response.setEmail(entity.getEmail());
    response.setMobile(entity.getMobile());
    response.setStatus(entity.getStatus());
    response.setCreatedAt(entity.getCreatedAt());
    response.setUpdatedAt(entity.getUpdatedAt());
    return response;
  }
}

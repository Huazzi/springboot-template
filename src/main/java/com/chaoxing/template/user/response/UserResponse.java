package com.chaoxing.template.user.response;

import com.chaoxing.template.user.entity.UserEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {

  private Long id;
  private String username;
  private String nickname;
  private String email;
  private String mobile;
  private Integer status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

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

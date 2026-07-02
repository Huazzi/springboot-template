package com.chaoxing.template.user.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEntity {

  private Long id;
  private String username;
  private String nickname;
  private String email;
  private String mobile;
  private Integer status;
  private Boolean deleted;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

package com.chaoxing.template.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

  @Size(max = 64, message = "昵称最长64字符") private String nickname;

  @Email(message = "邮箱格式不正确") @Size(max = 128, message = "邮箱最长128字符") private String email;

  @Size(max = 20, message = "手机号最长20字符") private String mobile;

  @Min(value = 0, message = "状态0或1") @Max(value = 1, message = "状态0或1") private Integer status;
}

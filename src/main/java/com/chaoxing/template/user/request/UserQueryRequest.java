package com.chaoxing.template.user.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserQueryRequest {

  @Min(value = 1, message = "页码最小为1") private long pageNo = 1;

  @Min(value = 1, message = "最小1") @Max(value = 100, message = "最大100") private long pageSize = 10;

  @Size(max = 64, message = "用户名最长64字符") private String username;

  @Size(max = 64, message = "昵称最长64字符") private String nickname;

  @Min(value = 0, message = "状态0或1") @Max(value = 1, message = "状态0或1") private Integer status;
}

package com.chaoxing.template.user.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 加载更多（无限滚动）分页请求。
 *
 * <p>与 {@link UserQueryRequest} 的区别：用游标 lastId 代替 pageNo， 客户端把上一页最后一条记录的 id 传回来，SQL 侧用 {@code WHERE
 * id < lastId} 直接跳过已经加载过的数据，深翻页性能恒定，不随翻页深度增长。 lastId 为空表示第一页。
 */
@Getter
@Setter
public class UserLoadMoreRequest {

  /** 上一页最后一条记录的 id，为空表示取第一页。 */
  @Positive(message = "游标ID必须为正整数") private Long lastId;

  @Min(value = 1, message = "最小1") @Max(value = 100, message = "最大100") private long pageSize = 10;

  @Size(max = 64, message = "用户名最长64字符") private String username;

  @Size(max = 64, message = "昵称最长64字符") private String nickname;

  @Min(value = 0, message = "状态0或1") @Max(value = 1, message = "状态0或1") private Integer status;
}

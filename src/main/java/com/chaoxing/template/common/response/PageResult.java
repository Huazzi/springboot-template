package com.chaoxing.template.common.response;

import java.util.List;
import lombok.Getter;

@Getter
public final class PageResult<T> {

  private final List<T> records;
  private final long total;
  private final long pageNo;
  private final long pageSize;
  private final long pages;

  /** 防御性复制列表，避免响应创建后被调用方继续修改。 */
  private PageResult(List<T> records, long total, long pageNo, long pageSize) {
    this.records = records == null ? List.of() : List.copyOf(records);
    this.total = Math.max(total, 0);
    this.pageNo = normalizePageNo(pageNo);
    this.pageSize = normalizePageSize(pageSize);
    this.pages = calculatePages(this.total, this.pageSize);
  }

  /** 在分页响应边界统一修正非法分页参数，让调用方拿到稳定结构。 */
  public static <T> PageResult<T> of(List<T> records, long total, long pageNo, long pageSize) {
    return new PageResult<>(records, total, pageNo, pageSize);
  }

  public static <T> PageResult<T> empty(long pageNo, long pageSize) {
    return new PageResult<>(List.of(), 0, pageNo, pageSize);
  }

  private static long normalizePageNo(long pageNo) {
    return pageNo < 1 ? 1 : pageNo;
  }

  private static long normalizePageSize(long pageSize) {
    return pageSize < 1 ? 10 : pageSize;
  }

  private static long calculatePages(long total, long pageSize) {
    if (total == 0) {
      return 0;
    }
    return (total + pageSize - 1) / pageSize;
  }
}

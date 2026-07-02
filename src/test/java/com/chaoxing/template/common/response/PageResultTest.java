package com.chaoxing.template.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {

  @Test
  void shouldBuildPageResultAndCalculatePages() {
    PageResult<String> result = PageResult.of(List.of("a", "b"), 21, 2, 10);

    assertThat(result.getRecords()).containsExactly("a", "b");
    assertThat(result.getTotal()).isEqualTo(21);
    assertThat(result.getPageNo()).isEqualTo(2);
    assertThat(result.getPageSize()).isEqualTo(10);
    assertThat(result.getPages()).isEqualTo(3);
  }

  @Test
  void shouldNormalizeInvalidPageArguments() {
    PageResult<String> result = PageResult.of(null, -1, 0, 0);

    assertThat(result.getRecords()).isEmpty();
    assertThat(result.getTotal()).isZero();
    assertThat(result.getPageNo()).isEqualTo(1);
    assertThat(result.getPageSize()).isEqualTo(10);
    assertThat(result.getPages()).isZero();
  }
}

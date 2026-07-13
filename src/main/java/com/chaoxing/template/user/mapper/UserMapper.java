package com.chaoxing.template.user.mapper;

import com.chaoxing.template.user.entity.UserEntity;
import com.chaoxing.template.user.request.UserQueryRequest;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

  int insert(UserEntity entity);

  /** 默认查询都会忽略已逻辑删除的数据。 */
  UserEntity selectById(Long id);

  /**
   * 批量一次性将一批 id 查询出来
   * @param ids 用户 id 列表
   * @return 用户实体列表
   */
  List<UserEntity> selectByIds(@Param("ids") List<Long> ids);

  /** 插入前检查用于返回友好提示，数据库唯一索引仍是最终兜底。 */
  int countByUsername(String username);

  long countByCondition(@Param("query") UserQueryRequest query);

  List<UserEntity> selectPage(
      @Param("query") UserQueryRequest query,
      @Param("offset") long offset,
      @Param("pageSize") long pageSize);

  int updateById(UserEntity entity);

  /** 逻辑删除保留历史数据，同时让默认查询不再返回该记录。 */
  int logicDeleteById(Long id);
}

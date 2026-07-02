package com.chaoxing.template.user.mapper;

import com.chaoxing.template.user.entity.UserEntity;
import com.chaoxing.template.user.request.UserQueryRequest;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

  int insert(UserEntity entity);

  UserEntity selectById(Long id);

  int countByUsername(String username);

  long countByCondition(@Param("query") UserQueryRequest query);

  List<UserEntity> selectPage(
      @Param("query") UserQueryRequest query,
      @Param("offset") long offset,
      @Param("pageSize") long pageSize);

  int updateById(UserEntity entity);

  int logicDeleteById(Long id);
}

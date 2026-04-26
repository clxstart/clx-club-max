package com.clx.user.mapper;

import com.clx.user.vo.ActiveUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 活跃用户 Mapper。
 */
@Mapper
public interface ActiveMapper {

    /**
     * 查询活跃用户排行。
     * 活跃度分数 = 获赞数 * 3 + 粉丝数 * 2 + 关注数 * 1
     */
    List<ActiveUserVO> selectActiveUsers(@Param("limit") int limit);
}
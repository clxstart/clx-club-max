package com.clx.analytics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.clx.analytics.entity.BehaviorLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 行为日志 Mapper
 */
@Mapper
public interface BehaviorLogMapper extends BaseMapper<BehaviorLog> {
}

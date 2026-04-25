package com.clx.search.mapper;

import com.clx.search.entity.SearchLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * 搜索日志 Mapper。
 */
@Mapper
public interface SearchLogMapper {

    @Insert("INSERT INTO search_log (keyword, user_id, search_types, result_count, cost_time, click_results, ip, user_agent, create_time) " +
            "VALUES (#{keyword}, #{userId}, #{searchTypes}, #{resultCount}, #{costTime}, #{clickResults}, #{ip}, #{userAgent}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SearchLog searchLog);
}
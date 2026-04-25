package com.clx.search.mapper;

import com.clx.search.entity.HotKeyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 搜索热词 Mapper。
 */
@Mapper
public interface HotKeywordMapper {

    @Select("SELECT * FROM hot_keyword WHERE period_type = #{periodType} AND period_date = #{periodDate} ORDER BY search_count DESC LIMIT #{limit}")
    List<HotKeyword> getHotKeywords(@Param("periodType") String periodType, @Param("periodDate") String periodDate, @Param("limit") int limit);

    @Update("UPDATE hot_keyword SET search_count = search_count + 1, update_time = NOW() WHERE keyword = #{keyword} AND period_type = #{periodType} AND period_date = #{periodDate}")
    int incrementCount(@Param("keyword") String keyword, @Param("periodType") String periodType, @Param("periodDate") String periodDate);
}
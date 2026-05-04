package com.clx.admin.mapper;

import com.clx.admin.dto.LogQueryDTO;
import com.clx.admin.vo.OperLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志 Mapper。
 */
@Mapper
public interface OperLogMapper {

    /**
     * 插入操作日志。
     */
    int insert(OperLogDTO log);

    /**
     * 分页查询操作日志。
     */
    List<OperLogVO> selectPage(@Param("query") LogQueryDTO query);

    /**
     * 查询总数。
     */
    long selectCount(@Param("query") LogQueryDTO query);

    /**
     * 操作日志 DTO。
     */
    @lombok.Data
    class OperLogDTO {
        private Long id;
        private Long userId;
        private String username;
        private String module;
        private String action;
        private String method;
        private String requestUrl;
        private String requestMethod;
        private String requestParams;
        private String responseResult;
        private String status;
        private String errorMsg;
        private Long costTime;
        private String operIp;
    }
}
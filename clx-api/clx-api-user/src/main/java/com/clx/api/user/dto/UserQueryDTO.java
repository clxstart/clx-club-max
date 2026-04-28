package com.clx.api.user.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户分页查询条件 DTO。
 */
@Data
public class UserQueryDTO {

    /** 页码，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 10 */
    private Integer size = 10;

    /** 用户名模糊搜索 */
    private String username;

    /** 用户 ID 精确匹配 */
    private Long userId;

    /** 状态筛选：0正常，1封禁 */
    private String status;
}
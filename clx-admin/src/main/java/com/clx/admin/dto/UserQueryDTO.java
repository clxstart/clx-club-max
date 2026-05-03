package com.clx.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户分页查询条件。
 */
@Data
public class UserQueryDTO {

    /** 页码 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 10;

    /** 用户名模糊搜索 */
    private String username;

    /** 用户 ID */
    private Long userId;

    /** 状态：0正常，1封禁 */
    private String status;
}

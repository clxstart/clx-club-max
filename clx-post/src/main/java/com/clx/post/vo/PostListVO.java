package com.clx.post.vo;

import lombok.Data;

import java.util.List;

/**
 * 帖子列表响应VO。
 */
@Data
public class PostListVO {

    /** 帖子列表 */
    private List<PostListItemVO> posts;

    /** 总数 */
    private Integer total;

    /** 当前页 */
    private Integer page;

    /** 每页数量 */
    private Integer size;
}
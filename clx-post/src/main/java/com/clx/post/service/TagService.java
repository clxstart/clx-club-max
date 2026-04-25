package com.clx.post.service;

import com.clx.post.vo.TagVO;

import java.util.List;

/**
 * 标签服务接口。
 */
public interface TagService {

    /**
     * 获取所有标签。
     */
    List<TagVO> getAll();
}
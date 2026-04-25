package com.clx.quiz.service;

import com.clx.quiz.vo.WrongBookVO;

import java.util.List;
import java.util.Map;

/**
 * 错题本服务接口。
 */
public interface WrongBookService {

    /**
     * 添加错题。
     *
     * @param userId    用户ID
     * @param subjectId 题目ID
     */
    void add(Long userId, Long subjectId);

    /**
     * 分页查询错题本。
     *
     * @param userId   用户ID
     * @param pageNo   页码
     * @param pageSize 每页数量
     * @return 错题列表和总数
     */
    Map<String, Object> list(Long userId, int pageNo, int pageSize);

    /**
     * 移除错题。
     *
     * @param userId    用户ID
     * @param subjectId 题目ID
     * @return 是否成功
     */
    boolean remove(Long userId, Long subjectId);
}
package com.clx.quiz.service;

import com.clx.quiz.dto.PracticeStartRequest;
import com.clx.quiz.dto.PracticeSubmitRequest;
import com.clx.quiz.vo.PracticeResultVO;
import com.clx.quiz.vo.PracticeSubjectVO;
import com.clx.quiz.vo.SubmitResultVO;

import java.util.List;
import java.util.Map;

/**
 * 练习服务接口。
 */
public interface PracticeService {

    /**
     * 开始练习（随机组题）。
     *
     * @param request 请求参数
     * @param userId   用户ID
     * @return 练习ID和题目ID列表
     */
    Map<String, Object> start(PracticeStartRequest request, Long userId);

    /**
     * 获取练习题目（不含答案）。
     */
    PracticeSubjectVO getSubject(Long practiceId, Long subjectId, Integer subjectType);

    /**
     * 提交答案。
     */
    SubmitResultVO submit(PracticeSubmitRequest request, Long userId);

    /**
     * 简答题自评。
     */
    boolean selfJudge(Long practiceId, Long subjectId, Integer isCorrect, Long userId);

    /**
     * 结束练习。
     */
    PracticeResultVO finish(Long practiceId, Long userId);
}
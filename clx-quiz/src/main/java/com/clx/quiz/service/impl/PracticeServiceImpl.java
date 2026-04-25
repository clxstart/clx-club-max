package com.clx.quiz.service.impl;

import com.clx.quiz.dto.PracticeStartRequest;
import com.clx.quiz.dto.PracticeSubmitRequest;
import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.entity.Practice;
import com.clx.quiz.entity.PracticeDetail;
import com.clx.quiz.entity.Subject;
import com.clx.quiz.mapper.PracticeDetailMapper;
import com.clx.quiz.mapper.PracticeMapper;
import com.clx.quiz.mapper.SubjectMapper;
import com.clx.quiz.service.PracticeService;
import com.clx.quiz.service.SubjectService;
import com.clx.quiz.service.WrongBookService;
import com.clx.quiz.service.handler.SubjectTypeHandler;
import com.clx.quiz.service.handler.SubjectTypeHandlerFactory;
import com.clx.quiz.vo.PracticeResultVO;
import com.clx.quiz.vo.PracticeSubjectVO;
import com.clx.quiz.vo.SubmitResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 练习服务实现。
 */
@Service
@RequiredArgsConstructor
public class PracticeServiceImpl implements PracticeService {

    private final PracticeMapper practiceMapper;
    private final PracticeDetailMapper detailMapper;
    private final SubjectMapper subjectMapper;
    private final SubjectService subjectService;
    private final SubjectTypeHandlerFactory handlerFactory;
    private final WrongBookService wrongBookService;

    // 存储练习开始时间（内存中，简单实现）
    private final Map<Long, LocalDateTime> startTimeMap = new HashMap<>();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> start(PracticeStartRequest request, Long userId) {
        // 随机获取题目ID
        List<Long> subjectIds = subjectService.getRandomSubjectIds(request.getLabelIds(), request.getCount());
        if (subjectIds.isEmpty()) {
            throw new RuntimeException("暂无符合条件的题目");
        }

        // 创建练习记录
        Practice practice = new Practice();
        practice.setUserId(userId);
        practice.setTotalCount(subjectIds.size());
        practice.setStatus(0); // 进行中
        practice.setCreatedBy(String.valueOf(userId));
        practiceMapper.insert(practice);

        Long practiceId = practice.getId();

        // 创建练习详情（每道题）
        List<PracticeDetail> details = new ArrayList<>();
        for (Long subjectId : subjectIds) {
            Subject subject = subjectMapper.selectById(subjectId);
            PracticeDetail detail = new PracticeDetail();
            detail.setPracticeId(practiceId);
            detail.setSubjectId(subjectId);
            detail.setSubjectType(subject.getSubjectType());
            detail.setCreatedBy(String.valueOf(userId));
            details.add(detail);
        }
        detailMapper.batchInsert(details);

        // 记录开始时间
        startTimeMap.put(practiceId, LocalDateTime.now());

        Map<String, Object> result = new HashMap<>();
        result.put("practiceId", practiceId);
        result.put("totalCount", subjectIds.size());
        result.put("subjectIds", subjectIds);
        return result;
    }

    @Override
    public PracticeSubjectVO getSubject(Long practiceId, Long subjectId, Integer subjectType) {
        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null) {
            return null;
        }

        PracticeSubjectVO vo = new PracticeSubjectVO();
        vo.setSubjectId(subjectId);
        vo.setSubjectName(subject.getSubjectName());
        vo.setSubjectType(subject.getSubjectType());
        vo.setSubjectDifficult(subject.getSubjectDifficult());

        // 获取选项（不含答案）
        SubjectTypeHandler handler = handlerFactory.getHandler(subjectType);
        vo.setOptionList(handler.getWithoutAnswer(subjectId));

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitResultVO submit(PracticeSubmitRequest request, Long userId) {
        // 获取题目
        Subject subject = subjectMapper.selectById(request.getSubjectId());
        if (subject == null) {
            throw new RuntimeException("题目不存在");
        }

        // 判题
        SubjectTypeHandler handler = handlerFactory.getHandler(request.getSubjectType());
        int isCorrect = handler.judge(request.getSubjectId(), request.getAnswerContent());

        // 更新练习详情
        PracticeDetail detail = detailMapper.selectByPracticeIdAndSubjectId(
                request.getPracticeId(), request.getSubjectId());
        if (detail != null) {
            detail.setAnswerContent(request.getAnswerContent());
            detail.setIsCorrect(isCorrect);
            detail.setUpdateBy(String.valueOf(userId));
            detailMapper.update(detail);
        }

        // 构造返回结果
        SubmitResultVO vo = new SubmitResultVO();
        vo.setIsCorrect(isCorrect);
        vo.setCorrectAnswer(handler.getCorrectAnswer(request.getSubjectId()));
        vo.setSubjectParse(subject.getSubjectParse());
        vo.setNeedSelfJudge(isCorrect == 2); // 简答题需要自评

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean selfJudge(Long practiceId, Long subjectId, Integer isCorrect, Long userId) {
        PracticeDetail detail = detailMapper.selectByPracticeIdAndSubjectId(practiceId, subjectId);
        if (detail == null) {
            return false;
        }
        detail.setIsCorrect(isCorrect);
        detail.setUpdateBy(String.valueOf(userId));
        return detailMapper.update(detail) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticeResultVO finish(Long practiceId, Long userId) {
        // 获取练习详情
        List<PracticeDetail> details = detailMapper.selectByPracticeId(practiceId);

        // 统计正确数和错题
        int correctCount = 0;
        List<Long> wrongSubjectIds = new ArrayList<>();
        for (PracticeDetail detail : details) {
            if (detail.getIsCorrect() != null && detail.getIsCorrect() == 1) {
                correctCount++;
            } else if (detail.getIsCorrect() != null && detail.getIsCorrect() == 0) {
                wrongSubjectIds.add(detail.getSubjectId());
            }
        }

        // 计算正确率
        int totalCount = details.size();
        BigDecimal correctRate = BigDecimal.ZERO;
        if (totalCount > 0) {
            correctRate = new BigDecimal(correctCount)
                    .divide(new BigDecimal(totalCount), 2, RoundingMode.HALF_UP);
        }

        // 计算用时
        LocalDateTime startTime = startTimeMap.getOrDefault(practiceId, LocalDateTime.now());
        long seconds = ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
        String timeUsed = formatTimeUsed(seconds);

        // 更新练习记录
        Practice practice = practiceMapper.selectById(practiceId);
        if (practice != null) {
            practice.setCorrectCount(correctCount);
            practice.setCorrectRate(correctRate);
            practice.setTimeUsed((int) seconds);
            practice.setStatus(1); // 已完成
            practice.setUpdateBy(String.valueOf(userId));
            practiceMapper.update(practice);
        }

        // 写入错题本
        for (Long subjectId : wrongSubjectIds) {
            wrongBookService.add(userId, subjectId);
        }

        // 清除开始时间
        startTimeMap.remove(practiceId);

        // 返回结果
        PracticeResultVO vo = new PracticeResultVO();
        vo.setTotalCount(totalCount);
        vo.setCorrectCount(correctCount);
        vo.setCorrectRate(correctRate.doubleValue());
        vo.setTimeUsed(timeUsed);
        vo.setWrongSubjectIds(wrongSubjectIds);
        return vo;
    }

    private String formatTimeUsed(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
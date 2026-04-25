package com.clx.quiz.service.impl;

import com.clx.quiz.entity.Subject;
import com.clx.quiz.entity.WrongBook;
import com.clx.quiz.mapper.SubjectMapper;
import com.clx.quiz.mapper.WrongBookMapper;
import com.clx.quiz.service.WrongBookService;
import com.clx.quiz.vo.WrongBookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 错题本服务实现。
 */
@Service
@RequiredArgsConstructor
public class WrongBookServiceImpl implements WrongBookService {

    private final WrongBookMapper wrongBookMapper;
    private final SubjectMapper subjectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long userId, Long subjectId) {
        // 查询是否已存在
        WrongBook existing = wrongBookMapper.selectByUserAndSubject(userId, subjectId);

        if (existing != null) {
            // 已存在，累加次数
            existing.setWrongCount(existing.getWrongCount() + 1);
            existing.setLastWrongTime(LocalDateTime.now());
            existing.setUpdateBy(String.valueOf(userId));
            wrongBookMapper.update(existing);
        } else {
            // 不存在，新增
            WrongBook wrongBook = new WrongBook();
            wrongBook.setUserId(userId);
            wrongBook.setSubjectId(subjectId);
            wrongBook.setWrongCount(1);
            wrongBook.setLastWrongTime(LocalDateTime.now());
            wrongBook.setCreatedBy(String.valueOf(userId));
            wrongBookMapper.insert(wrongBook);
        }
    }

    @Override
    public Map<String, Object> list(Long userId, int pageNo, int pageSize) {
        int offset = (pageNo - 1) * pageSize;
        List<WrongBook> wrongBooks = wrongBookMapper.selectPage(userId, offset, pageSize);
        int total = wrongBookMapper.count(userId);

        // 查询题目信息
        List<WrongBookVO> voList = wrongBooks.stream().map(wb -> {
            Subject subject = subjectMapper.selectById(wb.getSubjectId());
            WrongBookVO vo = new WrongBookVO();
            vo.setSubjectId(wb.getSubjectId());
            if (subject != null) {
                vo.setSubjectName(subject.getSubjectName());
                vo.setSubjectType(subject.getSubjectType());
            }
            vo.setWrongCount(wb.getWrongCount());
            vo.setLastWrongTime(wb.getLastWrongTime());
            return vo;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", voList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(Long userId, Long subjectId) {
        WrongBook wrongBook = wrongBookMapper.selectByUserAndSubject(userId, subjectId);
        if (wrongBook == null) {
            return false;
        }
        return wrongBookMapper.deleteById(wrongBook.getId()) > 0;
    }
}
package com.clx.quiz.service.handler;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题型处理策略工厂。
 */
@Component
public class SubjectTypeHandlerFactory {

    private final Map<Integer, SubjectTypeHandler> handlerMap = new HashMap<>();

    /**
     * 通过构造器注入所有 Handler 实现，自动注册到 Map 中。
     */
    public SubjectTypeHandlerFactory(List<SubjectTypeHandler> handlers) {
        for (SubjectTypeHandler handler : handlers) {
            handlerMap.put(handler.getType(), handler);
        }
    }

    /**
     * 根据题型获取对应的 Handler。
     *
     * @param subjectType 题型：1单选 2多选 3判断 4简答
     * @return Handler 实例
     */
    public SubjectTypeHandler getHandler(int subjectType) {
        SubjectTypeHandler handler = handlerMap.get(subjectType);
        if (handler == null) {
            throw new IllegalArgumentException("不支持的题目类型: " + subjectType);
        }
        return handler;
    }
}
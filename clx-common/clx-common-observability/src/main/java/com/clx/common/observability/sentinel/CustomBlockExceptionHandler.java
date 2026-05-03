package com.clx.common.observability.sentinel;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel 自定义限流/熔断异常处理器。
 *
 * <p>当触发限流或熔断时，返回统一的 JSON 格式响应。
 */
@Component
@RequiredArgsConstructor
public class CustomBlockExceptionHandler implements BlockExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, BlockException e) throws Exception {
        String message = getBlockMessage(e);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 429);
        result.put("msg", message);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());

        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * 根据异常类型返回对应消息。
     */
    private String getBlockMessage(BlockException e) {
        if (e instanceof FlowException) {
            return "访问过于频繁，请稍后重试";
        }
        if (e instanceof DegradeException) {
            return "服务已熔断，请稍后重试";
        }
        if (e instanceof ParamFlowException) {
            return "热点参数限流";
        }
        if (e instanceof SystemBlockException) {
            return "系统负载过高，请稍后重试";
        }
        if (e instanceof AuthorityException) {
            return "访问权限不足";
        }
        return "限流";
    }
}

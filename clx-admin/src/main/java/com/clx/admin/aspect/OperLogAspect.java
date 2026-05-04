package com.clx.admin.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.clx.admin.annotation.OperLog;
import com.clx.admin.service.OperLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志切面。
 *
 * <p>拦截所有带 @OperLog 注解的方法，自动记录操作日志。
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperLogAspect {

    private final OperLogService operLogService;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        String status = "0";
        String errorMsg = null;
        Object result = null;

        // 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        String requestUrl = request != null ? request.getRequestURI() : "";
        String requestMethod = request != null ? request.getMethod() : "";

        // 获取当前用户信息
        Long userId = null;
        String username = null;
        String operIp = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
                username = StpUtil.getLoginIdAsString();
                operIp = request != null ? request.getRemoteAddr() : null;
            }
        } catch (Exception e) {
            log.debug("获取用户信息失败: {}", e.getMessage());
        }

        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            status = "1";
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - start;

            // 获取请求参数
            String requestParams = null;
            if (operLog.recordParam() && point.getArgs().length > 0) {
                requestParams = getParams(point);
            }

            // 获取响应结果
            String responseResult = null;
            if (operLog.recordResult() && result != null) {
                try {
                    responseResult = JSON.toJSONString(result);
                } catch (Exception e) {
                    log.debug("序列化响应结果失败: {}", e.getMessage());
                }
            }

            // 异步记录日志
            if (userId != null) {
                operLogService.logAsync(
                    operLog.module(),
                    operLog.action(),
                    requestUrl,
                    requestMethod,
                    requestParams,
                    responseResult,
                    status,
                    errorMsg,
                    costTime,
                    userId,
                    username,
                    operIp
                );
            }
        }
    }

    /** 获取当前请求 */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    /** 获取请求参数 */
    private String getParams(ProceedingJoinPoint point) {
        try {
            Object[] args = point.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }
            // 过滤掉 HttpServletRequest/HttpServletResponse 等 Servlet 对象
            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof HttpServletRequest
                    || arg instanceof jakarta.servlet.http.HttpServletResponse
                    || arg instanceof org.springframework.web.multipart.MultipartFile) {
                    continue;
                }
                params.put("arg" + i, arg);
            }
            return params.isEmpty() ? null : JSON.toJSONString(params);
        } catch (Exception e) {
            log.debug("获取请求参数失败: {}", e.getMessage());
            return null;
        }
    }
}

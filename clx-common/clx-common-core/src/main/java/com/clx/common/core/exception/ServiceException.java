package com.clx.common.core.exception;

import com.clx.common.core.code.ResponseCode;
import lombok.Getter;

/**
 * 业务异常。
 *
 * <p>使用 ResponseCode 枚举确保错误码统一。
 */
@Getter
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public ServiceException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    public ServiceException(ResponseCode responseCode, String customMessage) {
        super(customMessage);
        this.code = responseCode.getCode();
    }

    public ServiceException(ResponseCode responseCode, Throwable cause) {
        super(responseCode.getMessage(), cause);
        this.code = responseCode.getCode();
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public ServiceException(String message) {
        this(ResponseCode.BAD_REQUEST, message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResponseCode.BAD_REQUEST.getCode();
    }

    // ========== 静态工厂方法 ==========

    public static ServiceException of(ResponseCode code) {
        return new ServiceException(code);
    }

    public static ServiceException of(String message) {
        return new ServiceException(message);
    }

    public static ServiceException of(int code, String message) {
        return new ServiceException(code, message);
    }

    // ========== 常用异常快捷创建 ==========

    public static ServiceException notFound(String resource) {
        return new ServiceException(ResponseCode.NOT_FOUND, resource + "不存在");
    }

    public static ServiceException alreadyExists(String resource) {
        return new ServiceException(ResponseCode.CONFLICT, resource + "已存在");
    }

    public static ServiceException forbidden() {
        return new ServiceException(ResponseCode.FORBIDDEN);
    }

    public static ServiceException validationFailed(String message) {
        return new ServiceException(ResponseCode.VALIDATION_FAILED, message);
    }

    public static ServiceException badRequest(String message) {
        return new ServiceException(ResponseCode.BAD_REQUEST, message);
    }

    public static ServiceException unauthorized() {
        return new ServiceException(ResponseCode.UNAUTHORIZED);
    }
}
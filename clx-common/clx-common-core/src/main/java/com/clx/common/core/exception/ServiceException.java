package com.clx.common.core.exception;

import lombok.Getter;

/**
 * 业务异常。
 */
@Getter
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;

    public ServiceException(String message) {
        this(400, message);
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(String message, Throwable cause) {
        this(400, message, cause);
    }

    public ServiceException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public static ServiceException of(String message) {
        return new ServiceException(message);
    }

    public static ServiceException of(int code, String message) {
        return new ServiceException(code, message);
    }

    public static ServiceException notFound(String resource) {
        return new ServiceException(404, resource + "不存在");
    }

    public static ServiceException alreadyExists(String resource) {
        return new ServiceException(409, resource + "已存在");
    }

    public static ServiceException forbidden() {
        return new ServiceException(403, "没有操作权限");
    }

    public static ServiceException validationFailed(String message) {
        return new ServiceException(400, message);
    }
}

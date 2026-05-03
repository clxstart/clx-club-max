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

    /**
     * 构造函数（使用默认错误码 BAD_REQUEST）。
     *
     * <p>注意：此构造函数使用 400 作为错误码，适用于参数校验失败等场景。
     * 对于其他业务错误，建议使用 {@link #ServiceException(ResponseCode)} 或 {@link #of(int, String)}。
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
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
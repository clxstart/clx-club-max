package com.clx.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应封装
 * 所有API接口统一返回格式
 *
 * @param <T> 数据类型
 */
@Data
@Schema(description = "统一响应结果")
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成功状态码 */
    public static final int SUCCESS = 200;

    /** 失败状态码 */
    public static final int FAIL = 500;

    /** 状态码 */
    @Schema(description = "状态码")
    private int code;

    /** 消息 */
    @Schema(description = "消息")
    private String msg;

    /** 数据 */
    @Schema(description = "数据")
    private T data;

    /**
     * 时间戳。
     *
     * <p>在对象创建时自动设置为当前时间。
     * 注意：如果 R 对象被序列化后反序列化，时间戳不会重新生成。
     * 对于需要精确时间戳的场景，建议在构造时显式设置。
     */
    @Schema(description = "时间戳")
    private long timestamp = System.currentTimeMillis();

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ========== 成功静态方法 ==========

    public static <T> R<T> ok() {
        return new R<>(SUCCESS, "操作成功", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS, "操作成功", data);
    }

    public static <T> R<T> ok(String msg, T data) {
        return new R<>(SUCCESS, msg, data);
    }

    // ========== 失败静态方法 ==========

    public static <T> R<T> fail() {
        return new R<>(FAIL, "操作失败", null);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(FAIL, msg, null);
    }

    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }

    public static <T> R<T> fail(int code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    // ========== 链式调用 ==========

    public R<T> code(int code) {
        this.code = code;
        return this;
    }

    public R<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public R<T> data(T data) {
        this.data = data;
        return this;
    }

    // ========== 判断方法 ==========

    public boolean isSuccess() {
        return SUCCESS == this.code;
    }

    public boolean isFail() {
        return !isSuccess();
    }

}

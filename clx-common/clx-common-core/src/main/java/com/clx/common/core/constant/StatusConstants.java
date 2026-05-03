package com.clx.common.core.constant;

/**
 * 通用状态常量。
 *
 * <p>用户状态：0=正常，1=禁用，2=锁定
 * <p>删除标记：0=未删除，1=已删除
 */
public final class StatusConstants {

    /** 用户状态 - 正常 */
    public static final String NORMAL = "0";

    /** 用户状态 - 禁用（管理员操作） */
    public static final String DISABLED = "1";

    /** 用户状态 - 锁定（系统自动，如密码错误过多） */
    public static final String LOCKED = "2";

    /** 删除标记 - 未删除 */
    public static final int NOT_DELETED = 0;

    /** 删除标记 - 已删除 */
    public static final int DELETED = 1;

    private StatusConstants() {
    }
}

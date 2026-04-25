package com.clx.search.enums;

import lombok.Getter;

/**
 * 搜索类型枚举。
 */
@Getter
public enum SearchTypeEnum {

    POST("帖子", "post"),
    USER("用户", "user"),
    CATEGORY("分类", "category"),
    TAG("标签", "tag"),
    PICTURE("图片", "picture"),
    WEB("网页", "web");

    private final String text;
    private final String value;

    SearchTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举。
     */
    public static SearchTypeEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (SearchTypeEnum type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为有效的搜索类型。
     */
    public static boolean isValid(String value) {
        return getByValue(value) != null;
    }
}
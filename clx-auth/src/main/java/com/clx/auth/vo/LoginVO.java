package com.clx.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录返回 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录返回结果")
public class LoginVO {

    @Schema(description = "Token")
    private String token;

    @Schema(description = "Token名称")
    private String tokenName;

}
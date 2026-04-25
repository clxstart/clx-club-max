package com.clx.auth.mapper;

import com.clx.auth.entity.SocialBind;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社交账号绑定 Mapper（sys_social_bind 表）
 * 保留同一个账号数据不丢失
 *
 * @author CLX
 * @since 2026-04-22
 */
@Mapper
public interface SocialBindMapper {

    /** 根据平台+社交ID查询绑定（OAuth登录时判断是否已绑定本地用户） */
    SocialBind selectBySocialTypeAndId(@Param("socialType") String socialType,
                                       @Param("socialId") String socialId);

    /** 查询用户所有绑定的第三方账号（个人中心展示用） */
    List<SocialBind> selectByUserId(@Param("userId") Long userId);

    /** 创建绑定关系（新用户OAuth登录 / 老用户绑定新平台） */
    int insert(SocialBind socialBind);

    /** 更新绑定信息（刷新token、更新头像昵称等） */
    int update(SocialBind socialBind);

    /** 删除绑定关系（用户解绑第三方账号） */
    int deleteById(@Param("id") Long id);

    /** 根据用户ID+平台类型查询绑定（检查是否已绑定某平台） */
    SocialBind selectByUserIdAndType(@Param("userId") Long userId,
                                     @Param("socialType") String socialType);
}

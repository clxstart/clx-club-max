package com.clx.auth.mapper;

import com.clx.auth.entity.SocialBind;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社交账号绑定数据访问 Mapper
 * <p>
 * 用途：操作 sys_social_bind 表，实现第三方账号的增删改查
 * <p>
 * 提供以下功能：
 * <ul>
 *   <li>根据平台类型和社交ID查询绑定关系</li>
 *   <li>根据用户ID查询所有绑定关系</li>
 *   <li>创建新的绑定关系</li>
 *   <li>更新绑定信息（如token）</li>
 *   <li>删除绑定关系</li>
 * </ul>
 *
 * @author CLX
 * @since 2026-04-22
 * @see SocialBind 实体类
 */
@Mapper
public interface SocialBindMapper {

    /**
     * 根据平台类型和社交ID查询绑定关系
     * <p>
     * 用途：OAuth登录时，判断这个第三方账号是否已绑定过本地用户
     * <p>
     * 查询条件：social_type + social_id（唯一索引）
     *
     * @param socialType 平台类型，如 "github"
     * @param socialId   第三方平台唯一标识，如 GitHub 的用户ID
     * @return 绑定关系，如果不存在返回 null
     */
    SocialBind selectBySocialTypeAndId(@Param("socialType") String socialType,
                                       @Param("socialId") String socialId);

    /**
     * 根据用户ID查询所有绑定的第三方账号
     * <p>
     * 用途：在个人中心展示用户绑定了哪些第三方账号
     *
     * @param userId 本地用户ID
     * @return 绑定关系列表
     */
    List<SocialBind> selectByUserId(@Param("userId") Long userId);

    /**
     * 创建新的绑定关系
     * <p>
     * 用途：
     * <ul>
     *   <li>新用户使用第三方登录时，创建绑定</li>
     *   <li>老用户绑定新的第三方账号</li>
     * </ul>
     *
     * @param socialBind 绑定关系对象
     * @return 影响行数
     */
    int insert(SocialBind socialBind);

    /**
     * 更新绑定信息
     * <p>
     * 用途：
     * <ul>
     *   <li>刷新 access_token</li>
     *   <li>更新头像、昵称等用户信息</li>
     * </ul>
     *
     * @param socialBind 绑定关系对象（包含要更新的字段）
     * @return 影响行数
     */
    int update(SocialBind socialBind);

    /**
     * 删除绑定关系
     * <p>
     * 用途：用户解绑第三方账号
     *
     * @param id 绑定关系ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户ID和平台类型查询绑定关系
     * <p>
     * 用途：检查用户是否已绑定某个特定平台
     *
     * @param userId     本地用户ID
     * @param socialType 平台类型，如 "github"
     * @return 绑定关系，如果不存在返回 null
     */
    SocialBind selectByUserIdAndType(@Param("userId") Long userId,
                                     @Param("socialType") String socialType);
}

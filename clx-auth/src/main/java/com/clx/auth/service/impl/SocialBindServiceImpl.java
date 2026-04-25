package com.clx.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.auth.entity.SocialBind;
import com.clx.auth.mapper.SocialBindMapper;
import com.clx.auth.service.SocialBindService;
import com.clx.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 社交账号绑定服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialBindServiceImpl implements SocialBindService {

    private final SocialBindMapper socialBindMapper;

    @Override
    public List<SocialBind> getMyBinds() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.debug("查询用户绑定的社交账号: userId={}", userId);
        return socialBindMapper.selectByUserId(userId);
    }

    @Override
    public boolean isBound(String platform) {
        Long userId = StpUtil.getLoginIdAsLong();
        SocialBind bind = socialBindMapper.selectByUserIdAndType(userId, platform);
        return bind != null;
    }

    @Override
    public SocialBind getBindByPlatform(String platform) {
        Long userId = StpUtil.getLoginIdAsLong();
        return socialBindMapper.selectByUserIdAndType(userId, platform);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long bindId) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("解绑社交账号: userId={}, bindId={}", userId, bindId);

        // 验证绑定关系属于当前用户
        List<SocialBind> binds = socialBindMapper.selectByUserId(userId);
        SocialBind bind = binds.stream()
                .filter(b -> b.getId().equals(bindId))
                .findFirst()
                .orElse(null);

        if (bind == null) {
            throw ServiceException.notFound("绑定关系");
        }

        int deleted = socialBindMapper.deleteById(bindId);
        if (deleted <= 0) {
            throw ServiceException.of(500, "解绑失败");
        }

        log.info("解绑成功: userId={}, socialType={}", userId, bind.getSocialType());
    }
}
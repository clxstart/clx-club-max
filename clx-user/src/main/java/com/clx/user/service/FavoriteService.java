package com.clx.user.service;

import com.clx.user.vo.FavoriteItemVO;

import java.util.List;

/**
 * 收藏服务接口。
 */
public interface FavoriteService {

    /**
     * 收藏帖子。
     */
    void addFavorite(Long userId, Long postId);

    /**
     * 取消收藏。
     */
    void removeFavorite(Long userId, Long postId);

    /**
     * 是否已收藏。
     */
    boolean isFavorited(Long userId, Long postId);

    /**
     * 获取收藏夹。
     */
    List<FavoriteItemVO> getFavorites(Long userId, int page, int size);

    /**
     * 统计收藏数。
     */
    int countFavorites(Long userId);
}
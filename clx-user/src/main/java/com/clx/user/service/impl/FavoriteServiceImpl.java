package com.clx.user.service.impl;

import com.clx.user.entity.PostFavorite;
import com.clx.user.mapper.PostFavoriteMapper;
import com.clx.user.service.FavoriteService;
import com.clx.user.vo.FavoriteItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 收藏服务实现。
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final PostFavoriteMapper favoriteMapper;
    private final JdbcTemplate jdbcTemplate; // 直接查询帖子表

    @Override
    public void addFavorite(Long userId, Long postId) {
        // 检查是否已收藏
        PostFavorite existing = favoriteMapper.selectByUserAndPost(userId, postId);
        if (existing != null) {
            throw new RuntimeException("已收藏该帖子");
        }
        // 插入收藏
        PostFavorite favorite = new PostFavorite();
        favorite.setUserId(userId);
        favorite.setPostId(postId);
        favoriteMapper.insert(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long postId) {
        // 检查是否已收藏
        PostFavorite existing = favoriteMapper.selectByUserAndPost(userId, postId);
        if (existing == null) {
            throw new RuntimeException("未收藏该帖子");
        }
        favoriteMapper.deleteByUserAndPost(userId, postId);
    }

    @Override
    public boolean isFavorited(Long userId, Long postId) {
        if (userId == null) {
            return false;
        }
        return favoriteMapper.selectByUserAndPost(userId, postId) != null;
    }

    @Override
    public List<FavoriteItemVO> getFavorites(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<Long> postIds = favoriteMapper.selectPostIds(userId, offset, size);
        List<FavoriteItemVO> result = new ArrayList<>();

        for (Long postId : postIds) {
            // 直接查询 clx_post.post 表（跨库查询，开发环境可用）
            try {
                String sql = "SELECT id, title, summary, author_name, like_count, create_time " +
                        "FROM clx_post.post WHERE id = ? AND is_deleted = 0";
                Map<String, Object> post = jdbcTemplate.queryForMap(sql, postId);
                if (post != null) {
                    // 查询收藏时间
                    PostFavorite fav = favoriteMapper.selectByUserAndPost(userId, postId);
                    result.add(new FavoriteItemVO(
                            postId,
                            (String) post.get("title"),
                            (String) post.get("summary"),
                            (String) post.get("author_name"),
                            (Integer) post.get("like_count"),
                            (java.time.LocalDateTime) post.get("create_time"),
                            fav != null ? fav.getCreateTime() : null
                    ));
                }
            } catch (Exception e) {
                // 帖子可能被删除，跳过
            }
        }
        return result;
    }

    @Override
    public int countFavorites(Long userId) {
        return favoriteMapper.count(userId);
    }
}
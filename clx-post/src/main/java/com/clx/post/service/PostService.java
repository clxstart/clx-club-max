package com.clx.post.service;

import com.clx.post.dto.PostCreateRequest;
import com.clx.post.dto.PostListRequest;
import com.clx.post.dto.PostUpdateRequest;
import com.clx.post.entity.Post;
import com.clx.post.vo.PostDetailVO;
import com.clx.post.vo.PostListVO;
import com.clx.post.vo.PostListItemVO;

import java.util.List;

/**
 * 帖子服务接口。
 */
public interface PostService {

    /**
     * 创建帖子。
     *
     * @param request 创建请求
     * @param userId   用户ID
     * @return 帖子ID
     */
    Long create(PostCreateRequest request, Long userId);

    /**
     * 更新帖子。
     *
     * @param postId  帖子ID
     * @param request 更新请求
     * @param userId   用户ID
     */
    void update(Long postId, PostUpdateRequest request, Long userId);

    /**
     * 删除帖子。
     *
     * @param postId 帖子ID
     * @param userId  用户ID
     */
    void delete(Long postId, Long userId);

    /**
     * 获取帖子详情。
     *
     * @param postId 帖子ID
     * @param userId  用户ID（可为空）
     * @return 帖子详情
     */
    PostDetailVO getDetail(Long postId, Long userId);

    /**
     * 获取帖子列表。
     *
     * @param request 查询请求
     * @return 帖子列表
     */
    PostListVO getList(PostListRequest request);

    /**
     * 搜索帖子。
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页数量
     * @return 帖子列表
     */
    PostListVO search(String keyword, Integer page, Integer size);

    /**
     * 获取热门帖子。
     *
     * @param limit 数量限制
     * @return 热门帖子列表
     */
    List<PostListItemVO> getHot(Integer limit);

    /**
     * 按作者查询帖子。
     *
     * @param authorId 作者ID
     * @param page     页码
     * @param size     每页数量
     * @return 帖子列表
     */
    PostListVO getByAuthor(Long authorId, Integer page, Integer size);

    // ========== 后台管理接口 ==========

    /** 根据ID获取帖子实体 */
    Post getById(Long postId);

    /** 更新帖子实体 */
    void update(Post post);

    /** 删除帖子 */
    void deleteById(Long postId);

    /** 分页查询帖子（后台管理） */
    List<Post> getPostPage(String title, Integer status, Long authorId, int offset, int pageSize);

    /** 统计帖子数量（后台管理） */
    int getPostPageCount(String title, Integer status, Long authorId);
}
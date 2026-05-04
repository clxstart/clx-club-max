package com.clx.post.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReUtil;
import com.clx.post.dto.PostCreateRequest;
import com.clx.post.dto.PostListRequest;
import com.clx.post.dto.PostUpdateRequest;
import com.clx.post.entity.Category;
import com.clx.post.entity.LikeRecord;
import com.clx.post.entity.Post;
import com.clx.post.entity.Tag;
import com.clx.post.mapper.*;
import com.clx.post.service.PostService;
import com.clx.post.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子服务实现。
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final PostTagMapper postTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final LikeRecordMapper likeRecordMapper;

    @Override
    @Transactional
    public Long create(PostCreateRequest request, Long userId) {
        // 构建帖子实体
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(CharSequenceUtil.maxLength(ReUtil.delAll("<[^>]+>", request.getContent()), 200));
        post.setAuthorId(userId);
        post.setStatus("0");
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setIsTop(false);
        post.setIsEssence(false);
        post.setIsDeleted(false);

        // 设置分类
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category != null) {
                post.setCategoryId(category.getId());
                post.setCategoryName(category.getName());
            }
        }

        // 插入帖子
        postMapper.insert(post);

        // 设置标签
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            postTagMapper.batchInsert(post.getId(), request.getTagIds());
        }

        return post.getId();
    }

    @Override
    @Transactional
    public void update(Long postId, PostUpdateRequest request, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权修改此帖子");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(CharSequenceUtil.maxLength(ReUtil.delAll("<[^>]+>", request.getContent()), 200));

        // 更新分类
        if (request.getCategoryId() != null) {
            Category category = categoryMapper.selectById(request.getCategoryId());
            if (category != null) {
                post.setCategoryId(category.getId());
                post.setCategoryName(category.getName());
            }
        } else {
            post.setCategoryId(null);
            post.setCategoryName(null);
        }

        postMapper.update(post);

        // 更新标签
        postTagMapper.deleteByPostId(postId);
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            postTagMapper.batchInsert(postId, request.getTagIds());
        }
    }

    @Override
    @Transactional
    public void delete(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        // 作者或管理员可删除
        if (!post.getAuthorId().equals(userId) && !StpUtil.hasRole("admin")) {
            throw new RuntimeException("无权删除此帖子");
        }

        postMapper.deleteById(postId);
        postTagMapper.deleteByPostId(postId);
    }

    @Override
    public PostDetailVO getDetail(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 增加浏览数
        postMapper.incrementViewCount(postId);

        // 获取标签
        List<Tag> tags = tagMapper.selectByPostId(postId);

        // 检查是否点赞
        boolean isLiked = false;
        if (userId != null) {
            isLiked = likeRecordMapper.exists(userId, "1", postId);
        }

        // 构建VO
        PostDetailVO vo = new PostDetailVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setViewCount(post.getViewCount());
        vo.setIsLiked(isLiked);
        vo.setCreatedAt(post.getCreateTime());

        // 作者信息
        PostDetailVO.AuthorVO authorVO = new PostDetailVO.AuthorVO();
        authorVO.setId(post.getAuthorId());
        authorVO.setName(post.getAuthorName());
        vo.setAuthor(authorVO);

        // 分类信息
        if (post.getCategoryId() != null) {
            PostDetailVO.CategoryVO categoryVO = new PostDetailVO.CategoryVO();
            categoryVO.setId(post.getCategoryId());
            categoryVO.setName(post.getCategoryName());
            vo.setCategory(categoryVO);
        }

        // 标签信息
        List<PostDetailVO.TagVO> tagVOs = tags.stream().map(tag -> {
            PostDetailVO.TagVO tagVO = new PostDetailVO.TagVO();
            tagVO.setId(tag.getId());
            tagVO.setName(tag.getName());
            tagVO.setColor(tag.getColor());
            return tagVO;
        }).collect(Collectors.toList());
        vo.setTags(tagVOs);

        return vo;
    }

    @Override
    public PostListVO getList(PostListRequest request) {
        int offset = (request.getPage() - 1) * request.getSize();

        List<Post> posts = postMapper.selectList(
                request.getCategoryId(),
                request.getTagId(),
                request.getSort(),
                offset,
                request.getSize()
        );

        int total = postMapper.selectCount(request.getCategoryId(), request.getTagId());

        List<PostListItemVO> postVOs = posts.stream().map(post -> {
            PostListItemVO vo = new PostListItemVO();
            vo.setId(post.getId());
            vo.setTitle(post.getTitle());
            vo.setSummary(post.getSummary());
            vo.setLikeCount(post.getLikeCount());
            vo.setCommentCount(post.getCommentCount());
            vo.setCreatedAt(post.getCreateTime());

            // 作者信息
            PostListItemVO.AuthorVO authorVO = new PostListItemVO.AuthorVO();
            authorVO.setId(post.getAuthorId());
            authorVO.setName(post.getAuthorName());
            vo.setAuthor(authorVO);

            // 分类信息
            if (post.getCategoryId() != null) {
                PostListItemVO.CategoryVO categoryVO = new PostListItemVO.CategoryVO();
                categoryVO.setId(post.getCategoryId());
                categoryVO.setName(post.getCategoryName());
                vo.setCategory(categoryVO);
            }

            // 标签信息
            List<Tag> tags = tagMapper.selectByPostId(post.getId());
            List<PostListItemVO.TagVO> tagVOs = tags.stream().map(tag -> {
                PostListItemVO.TagVO tagVO = new PostListItemVO.TagVO();
                tagVO.setId(tag.getId());
                tagVO.setName(tag.getName());
                tagVO.setColor(tag.getColor());
                return tagVO;
            }).collect(Collectors.toList());
            vo.setTags(tagVOs);

            return vo;
        }).collect(Collectors.toList());

        PostListVO result = new PostListVO();
        result.setPosts(postVOs);
        result.setTotal(total);
        result.setPage(request.getPage());
        result.setSize(request.getSize());

        return result;
    }

    @Override
    public PostListVO search(String keyword, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Post> posts = postMapper.search(keyword, offset, size);
        int total = postMapper.searchCount(keyword);

        List<PostListItemVO> postVOs = posts.stream().map(this::toListItemVO).collect(Collectors.toList());

        PostListVO result = new PostListVO();
        result.setPosts(postVOs);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    @Override
    public List<PostListItemVO> getHot(Integer limit) {
        List<Post> posts = postMapper.selectHot(limit);
        return posts.stream().map(this::toListItemVO).collect(Collectors.toList());
    }

    @Override
    public PostListVO getByAuthor(Long authorId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Post> posts = postMapper.selectByAuthor(authorId, offset, size);
        int total = postMapper.countByAuthor(authorId);

        List<PostListItemVO> postVOs = posts.stream().map(this::toListItemVO).collect(Collectors.toList());

        PostListVO result = new PostListVO();
        result.setPosts(postVOs);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    /**
     * Post实体转PostListItemVO。
     */
    private PostListItemVO toListItemVO(Post post) {
        PostListItemVO vo = new PostListItemVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setSummary(post.getSummary());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setCreatedAt(post.getCreateTime());

        PostListItemVO.AuthorVO authorVO = new PostListItemVO.AuthorVO();
        authorVO.setId(post.getAuthorId());
        authorVO.setName(post.getAuthorName());
        vo.setAuthor(authorVO);

        if (post.getCategoryId() != null) {
            PostListItemVO.CategoryVO categoryVO = new PostListItemVO.CategoryVO();
            categoryVO.setId(post.getCategoryId());
            categoryVO.setName(post.getCategoryName());
            vo.setCategory(categoryVO);
        }

        List<Tag> tags = tagMapper.selectByPostId(post.getId());
        List<PostListItemVO.TagVO> tagVOs = tags.stream().map(tag -> {
            PostListItemVO.TagVO tagVO = new PostListItemVO.TagVO();
            tagVO.setId(tag.getId());
            tagVO.setName(tag.getName());
            tagVO.setColor(tag.getColor());
            return tagVO;
        }).collect(Collectors.toList());
        vo.setTags(tagVOs);

        return vo;
    }

    // ========== 后台管理接口实现 ==========

    @Override
    public Post getById(Long postId) {
        return postMapper.selectById(postId);
    }

    @Override
    public void update(Post post) {
        postMapper.update(post);
    }

    @Override
    public void deleteById(Long postId) {
        postMapper.deleteById(postId);
    }

    @Override
    public List<Post> getPostPage(String title, Integer status, Long authorId, int offset, int pageSize) {
        return postMapper.selectAdminPage(title, status, authorId, offset, pageSize);
    }

    @Override
    public int getPostPageCount(String title, Integer status, Long authorId) {
        return postMapper.selectAdminPageCount(title, status, authorId);
    }
}
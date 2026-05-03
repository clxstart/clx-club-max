package com.clx.post.service;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.post.dto.PostCreateRequest;
import com.clx.post.dto.PostUpdateRequest;
import com.clx.post.entity.Category;
import com.clx.post.entity.Post;
import com.clx.post.entity.Tag;
import com.clx.post.mapper.CategoryMapper;
import com.clx.post.mapper.LikeRecordMapper;
import com.clx.post.mapper.PostMapper;
import com.clx.post.mapper.PostTagMapper;
import com.clx.post.mapper.TagMapper;
import com.clx.post.service.impl.PostServiceImpl;
import com.clx.post.vo.PostDetailVO;
import com.clx.post.vo.PostListVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 帖子服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostTagMapper postTagMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private LikeRecordMapper likeRecordMapper;

    @InjectMocks
    private PostServiceImpl postService;

    private Post buildPost() {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("测试帖子");
        post.setContent("这是测试内容");
        post.setSummary("这是测试内容");
        post.setAuthorId(100L);
        post.setAuthorName("admin");
        post.setCategoryId(10L);
        post.setCategoryName("技术");
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus("0");
        post.setIsTop(false);
        post.setIsEssence(false);
        post.setIsDeleted(false);
        post.setCreateTime(LocalDateTime.of(2026, 4, 22, 10, 0));
        return post;
    }

    @Nested
    @DisplayName("创建帖子")
    class Create {

        @Test
        @DisplayName("正常创建帖子 - 返回帖子ID")
        void create_success() {
            PostCreateRequest request = new PostCreateRequest();
            request.setTitle("新帖子");
            request.setContent("帖子内容");
            request.setCategoryId(10L);

            Category category = new Category();
            category.setId(10L);
            category.setName("技术");
            when(categoryMapper.selectById(10L)).thenReturn(category);

            // 模拟 insert 设置 ID
            when(postMapper.insert(any(Post.class))).thenAnswer((Answer<Integer>) invocation -> {
                Post post = invocation.getArgument(0);
                post.setId(1L);
                return 1;
            });

            Long postId = postService.create(request, 100L);

            assertEquals(1L, postId);
            verify(postMapper).insert(argThat(post ->
                    "新帖子".equals(post.getTitle()) &&
                    "帖子内容".equals(post.getContent()) &&
                    post.getAuthorId().equals(100L) &&
                    post.getCategoryId().equals(10L)
            ));
        }

        @Test
        @DisplayName("创建帖子 - 不设分类也能创建")
        void create_withoutCategory() {
            PostCreateRequest request = new PostCreateRequest();
            request.setTitle("无分类帖子");
            request.setContent("内容");

            when(postMapper.insert(any(Post.class))).thenAnswer((Answer<Integer>) invocation -> {
                Post post = invocation.getArgument(0);
                post.setId(2L);
                return 1;
            });

            Long postId = postService.create(request, 100L);

            assertEquals(2L, postId);
            verify(postMapper).insert(argThat(post ->
                    post.getCategoryId() == null
            ));
        }

        @Test
        @DisplayName("创建帖子 - 分类不存在时不设分类")
        void create_categoryNotExist() {
            PostCreateRequest request = new PostCreateRequest();
            request.setTitle("帖子");
            request.setContent("内容");
            request.setCategoryId(999L);

            when(categoryMapper.selectById(999L)).thenReturn(null);
            when(postMapper.insert(any(Post.class))).thenAnswer((Answer<Integer>) invocation -> {
                Post post = invocation.getArgument(0);
                post.setId(3L);
                return 1;
            });

            Long postId = postService.create(request, 100L);

            assertEquals(3L, postId);
            verify(postMapper).insert(argThat(post ->
                    post.getCategoryId() == null &&
                    post.getCategoryName() == null
            ));
        }

        @Test
        @DisplayName("创建帖子 - 带标签")
        void create_withTags() {
            PostCreateRequest request = new PostCreateRequest();
            request.setTitle("带标签帖子");
            request.setContent("内容");
            request.setTagIds(Arrays.asList(1L, 2L));

            when(postMapper.insert(any(Post.class))).thenAnswer((Answer<Integer>) invocation -> {
                Post post = invocation.getArgument(0);
                post.setId(4L);
                return 1;
            });

            postService.create(request, 100L);

            verify(postTagMapper).batchInsert(eq(4L), eq(Arrays.asList(1L, 2L)));
        }

        @Test
        @DisplayName("创建帖子 - HTML内容生成纯文本摘要")
        void create_summaryStripsHtml() {
            PostCreateRequest request = new PostCreateRequest();
            request.setTitle("HTML帖子");
            request.setContent("<p>Hello</p><b>World</b>");

            when(postMapper.insert(any(Post.class))).thenAnswer((Answer<Integer>) invocation -> {
                Post post = invocation.getArgument(0);
                post.setId(5L);
                return 1;
            });

            postService.create(request, 100L);

            verify(postMapper).insert(argThat(post ->
                    "HelloWorld".equals(post.getSummary())
            ));
        }
    }

    @Nested
    @DisplayName("更新帖子")
    class Update {

        @Test
        @DisplayName("正常更新帖子")
        void update_success() {
            Post existing = buildPost();
            when(postMapper.selectById(1L)).thenReturn(existing);

            PostUpdateRequest request = new PostUpdateRequest();
            request.setTitle("新标题");
            request.setContent("新内容");

            postService.update(1L, request, 100L);

            verify(postMapper).update(argThat(post ->
                    "新标题".equals(post.getTitle()) &&
                    "新内容".equals(post.getContent())
            ));
        }

        @Test
        @DisplayName("更新不存在的帖子 - 抛异常")
        void update_notExist() {
            when(postMapper.selectById(999L)).thenReturn(null);

            PostUpdateRequest request = new PostUpdateRequest();
            request.setTitle("新标题");
            request.setContent("新内容");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> postService.update(999L, request, 100L));
            assertEquals("帖子不存在", ex.getMessage());
        }

        @Test
        @DisplayName("更新他人帖子 - 抛异常")
        void update_notOwner() {
            Post existing = buildPost();
            when(postMapper.selectById(1L)).thenReturn(existing);

            PostUpdateRequest request = new PostUpdateRequest();
            request.setTitle("新标题");
            request.setContent("新内容");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> postService.update(1L, request, 200L));
            assertEquals("无权修改此帖子", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("删除帖子")
    class Delete {

        @Test
        @DisplayName("正常删除帖子")
        void delete_success() {
            Post existing = buildPost();
            when(postMapper.selectById(1L)).thenReturn(existing);

            postService.delete(1L, 100L);

            verify(postMapper).deleteById(1L);
            verify(postTagMapper).deleteByPostId(1L);
        }

        @Test
        @DisplayName("删除不存在的帖子 - 抛异常")
        void delete_notExist() {
            when(postMapper.selectById(999L)).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> postService.delete(999L, 100L));
            assertEquals("帖子不存在", ex.getMessage());
        }

        @Test
        @DisplayName("删除他人帖子 - 抛异常")
        void delete_notOwner() {
            Post existing = buildPost();
            when(postMapper.selectById(1L)).thenReturn(existing);

            // Mock StpUtil.hasRole 返回 false（非管理员）
            try (MockedStatic<StpUtil> mockedStpUtil = org.mockito.Mockito.mockStatic(StpUtil.class)) {
                mockedStpUtil.when(() -> StpUtil.hasRole("admin")).thenReturn(false);

                RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> postService.delete(1L, 200L));
                assertEquals("无权删除此帖子", ex.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("获取帖子详情")
    class GetDetail {

        @Test
        @DisplayName("正常获取详情")
        void getDetail_success() {
            Post post = buildPost();
            when(postMapper.selectById(1L)).thenReturn(post);
            when(tagMapper.selectByPostId(1L)).thenReturn(Collections.emptyList());

            PostDetailVO vo = postService.getDetail(1L, 100L);

            assertNotNull(vo);
            assertEquals(1L, vo.getId());
            assertEquals("测试帖子", vo.getTitle());
            verify(postMapper).incrementViewCount(1L);
        }

        @Test
        @DisplayName("帖子不存在 - 抛异常")
        void getDetail_notExist() {
            when(postMapper.selectById(999L)).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> postService.getDetail(999L, null));
            assertEquals("帖子不存在", ex.getMessage());
        }

        @Test
        @DisplayName("已登录用户 - 检查点赞状态")
        void getDetail_checkLikeStatus() {
            Post post = buildPost();
            when(postMapper.selectById(1L)).thenReturn(post);
            when(tagMapper.selectByPostId(1L)).thenReturn(Collections.emptyList());
            when(likeRecordMapper.exists(100L, "1", 1L)).thenReturn(true);

            PostDetailVO vo = postService.getDetail(1L, 100L);

            assertTrue(vo.getIsLiked());
        }

        @Test
        @DisplayName("未登录用户 - 点赞状态为false")
        void getDetail_anonymous_notLiked() {
            Post post = buildPost();
            when(postMapper.selectById(1L)).thenReturn(post);
            when(tagMapper.selectByPostId(1L)).thenReturn(Collections.emptyList());

            PostDetailVO vo = postService.getDetail(1L, null);

            assertFalse(vo.getIsLiked());
        }
    }

    @Nested
    @DisplayName("获取帖子列表")
    class GetList {

        @Test
        @DisplayName("分页参数正确传递")
        void getList_pagination() {
            Post post = buildPost();
            when(postMapper.selectList(isNull(), isNull(), anyString(), eq(20), eq(20)))
                    .thenReturn(Arrays.asList(post));
            when(postMapper.selectCount(isNull(), isNull())).thenReturn(25);
            when(tagMapper.selectByPostId(1L)).thenReturn(Collections.emptyList());

            com.clx.post.dto.PostListRequest request = new com.clx.post.dto.PostListRequest();
            request.setPage(2);
            request.setSize(20);

            PostListVO result = postService.getList(request);

            assertEquals(2, result.getPage());
            assertEquals(20, result.getSize());
            assertEquals(25, result.getTotal());
        }

        @Test
        @DisplayName("空列表返回正确结构")
        void getList_empty() {
            when(postMapper.selectList(isNull(), isNull(), anyString(), eq(0), eq(20)))
                    .thenReturn(Collections.emptyList());
            when(postMapper.selectCount(isNull(), isNull())).thenReturn(0);

            com.clx.post.dto.PostListRequest request = new com.clx.post.dto.PostListRequest();
            request.setPage(1);
            request.setSize(20);

            PostListVO result = postService.getList(request);

            assertNotNull(result.getPosts());
            assertTrue(result.getPosts().isEmpty());
            assertEquals(0, result.getTotal());
        }
    }
}

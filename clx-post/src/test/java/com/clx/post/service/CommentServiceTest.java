package com.clx.post.service;

import com.clx.post.dto.CommentCreateRequest;
import com.clx.post.entity.Comment;
import com.clx.post.mapper.CommentMapper;
import com.clx.post.mapper.LikeRecordMapper;
import com.clx.post.mapper.PostMapper;
import com.clx.post.service.impl.CommentServiceImpl;
import com.clx.post.vo.CommentVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 评论服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private LikeRecordMapper likeRecordMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Nested
    @DisplayName("创建评论")
    class Create {

        @Test
        @DisplayName("创建评论 - 返回评论ID")
        void create_success() {
            CommentCreateRequest request = new CommentCreateRequest();
            request.setContent("写得很好！");
            request.setParentId(null);

            when(commentMapper.insert(any(Comment.class))).thenAnswer((Answer<Integer>) invocation -> {
                Comment comment = invocation.getArgument(0);
                comment.setId(10L);
                return 1;
            });
            when(postMapper.incrementCommentCount(1L)).thenReturn(1);

            Long commentId = commentService.create(1L, request, 100L);

            assertEquals(10L, commentId);
            verify(commentMapper).insert(argThat(comment ->
                    "写得很好！".equals(comment.getContent()) &&
                    comment.getPostId().equals(1L) &&
                    comment.getAuthorId().equals(100L) &&
                    comment.getParentId().equals(0L)
            ));
            verify(postMapper).incrementCommentCount(1L);
        }

        @Test
        @DisplayName("创建子评论 - parentId正确传递")
        void create_reply() {
            CommentCreateRequest request = new CommentCreateRequest();
            request.setContent("回复内容");
            request.setParentId(5L);

            when(commentMapper.insert(any(Comment.class))).thenAnswer((Answer<Integer>) invocation -> {
                Comment comment = invocation.getArgument(0);
                comment.setId(11L);
                return 1;
            });
            when(postMapper.incrementCommentCount(1L)).thenReturn(1);

            commentService.create(1L, request, 100L);

            verify(commentMapper).insert(argThat(comment ->
                    comment.getParentId().equals(5L)
            ));
        }
    }

    @Nested
    @DisplayName("删除评论")
    class Delete {

        @Test
        @DisplayName("删除自己的评论")
        void delete_success() {
            Comment comment = new Comment();
            comment.setId(10L);
            comment.setPostId(1L);
            comment.setAuthorId(100L);

            when(commentMapper.selectById(10L)).thenReturn(comment);
            when(commentMapper.deleteById(10L)).thenReturn(1);
            when(postMapper.decrementCommentCount(1L)).thenReturn(1);

            commentService.delete(10L, 100L);

            verify(commentMapper).deleteById(10L);
            verify(postMapper).decrementCommentCount(1L);
        }

        @Test
        @DisplayName("删除不存在的评论 - 抛异常")
        void delete_notExist() {
            when(commentMapper.selectById(999L)).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> commentService.delete(999L, 100L));
            assertEquals("评论不存在", ex.getMessage());
        }

        @Test
        @DisplayName("删除他人评论 - 抛异常")
        void delete_notOwner() {
            Comment comment = new Comment();
            comment.setId(10L);
            comment.setPostId(1L);
            comment.setAuthorId(100L);

            when(commentMapper.selectById(10L)).thenReturn(comment);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> commentService.delete(10L, 200L));
            assertEquals("无权删除此评论", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("获取评论列表")
    class GetList {

        @Test
        @DisplayName("评论列表 - 已登录用户检查点赞状态")
        void getList_checkLikeStatus() {
            Comment comment = new Comment();
            comment.setId(10L);
            comment.setContent("测试评论");
            comment.setAuthorId(100L);
            comment.setAuthorName("admin");
            comment.setLikeCount(5);
            comment.setParentId(0L);

            when(commentMapper.selectByPostId(eq(1L), eq(0), eq(1000)))
                    .thenReturn(Arrays.asList(comment));
            when(likeRecordMapper.exists(100L, "2", 10L)).thenReturn(true);

            List<CommentVO> result = commentService.getList(1L, 100L);

            assertEquals(1, result.size());
            assertTrue(result.get(0).getIsLiked());
        }

        @Test
        @DisplayName("评论列表 - 未登录用户不检查点赞")
        void getList_anonymous_noLikeCheck() {
            Comment comment = new Comment();
            comment.setId(10L);
            comment.setContent("测试评论");
            comment.setAuthorId(100L);
            comment.setAuthorName("admin");
            comment.setLikeCount(5);
            comment.setParentId(0L);

            when(commentMapper.selectByPostId(eq(1L), eq(0), eq(1000)))
                    .thenReturn(Arrays.asList(comment));

            List<CommentVO> result = commentService.getList(1L, null);

            assertFalse(result.get(0).getIsLiked());
            verify(likeRecordMapper, never()).exists(any(), any(), any());
        }
    }
}

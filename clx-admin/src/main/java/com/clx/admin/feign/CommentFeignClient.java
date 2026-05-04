package com.clx.admin.feign;

import com.clx.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 评论服务 Feign 客户端。
 */
@FeignClient(name = "clx-post", contextId = "commentFeignClient", url = "${feign.client.config.clx-post.url:}")
public interface CommentFeignClient {

    /** 分页查询评论列表 */
    @PostMapping("/internal/comment/page")
    R<Map<String, Object>> getCommentPage(@RequestBody Map<String, Object> query);

    /** 删除评论 */
    @DeleteMapping("/internal/comment/{commentId}")
    R<Void> deleteComment(@PathVariable("commentId") Long commentId);
}
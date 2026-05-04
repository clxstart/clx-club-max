package com.clx.admin.feign;

import com.clx.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 帖子服务 Feign 客户端。
 */
@FeignClient(name = "clx-post", contextId = "postFeignClient", url = "${feign.client.config.clx-post.url:}")
public interface PostFeignClient {

    /** 分页查询帖子列表 */
    @PostMapping("/internal/post/page")
    R<Map<String, Object>> getPostPage(@RequestBody Map<String, Object> query);

    /** 获取帖子详情 */
    @GetMapping("/internal/post/{postId}")
    R<Map<String, Object>> getPostById(@PathVariable("postId") Long postId);

    /** 更新帖子状态 */
    @PutMapping("/internal/post/{postId}/status")
    R<Void> updatePostStatus(@PathVariable("postId") Long postId, @RequestParam Integer status);

    /** 删除帖子 */
    @DeleteMapping("/internal/post/{postId}")
    R<Void> deletePost(@PathVariable("postId") Long postId);
}

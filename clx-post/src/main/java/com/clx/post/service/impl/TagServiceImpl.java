package com.clx.post.service.impl;

import com.clx.post.entity.Tag;
import com.clx.post.mapper.TagMapper;
import com.clx.post.service.TagService;
import com.clx.post.vo.TagVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现。
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    @Override
    public List<TagVO> getAll() {
        List<Tag> tags = tagMapper.selectAll();
        return tags.stream().map(tag -> {
            TagVO vo = new TagVO();
            vo.setId(tag.getId());
            vo.setName(tag.getName());
            vo.setDescription(tag.getDescription());
            vo.setColor(tag.getColor());
            vo.setPostCount(0); // TODO: 统计帖子数量
            return vo;
        }).collect(Collectors.toList());
    }
}
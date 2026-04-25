package com.clx.post.service.impl;

import com.clx.post.entity.Category;
import com.clx.post.mapper.CategoryMapper;
import com.clx.post.service.CategoryService;
import com.clx.post.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现。
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> getAll() {
        List<Category> categories = categoryMapper.selectAll();
        return categories.stream().map(cat -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(cat.getId());
            vo.setName(cat.getName());
            vo.setCode(cat.getCode());
            vo.setDescription(cat.getDescription());
            vo.setIcon(cat.getIcon());
            vo.setPostCount(0); // TODO: 统计帖子数量
            return vo;
        }).collect(Collectors.toList());
    }
}
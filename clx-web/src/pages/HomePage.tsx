import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { postApi } from '../api';
import { PostListItem } from '../components/post/PostListItem';
import type { CategoryVO, PostListItemVO, TagVO } from '../api/types';

interface HomePageProps {
  categories: CategoryVO[];
  tags: TagVO[];
  activeCategory: string;  // 从顶部标签栏传入
  run: <T>(action: () => Promise<T>, ok: string) => Promise<T | undefined>;
}

// 首页：帖子列表（紧凑内容流）
export function HomePage({ categories, tags, activeCategory, run }: HomePageProps) {
  const navigate = useNavigate();
  const [posts, setPosts] = useState<PostListItemVO[]>([]);
  const [filters, setFilters] = useState({ page: 1, size: 20, sort: 'latest', tagId: '' });

  // 监听分类变化，刷新列表
  useEffect(() => {
    loadPosts();
  }, [activeCategory]);

  async function loadPosts() {
    const params = {
      ...filters,
      categoryId: activeCategory || undefined,
    };
    const data = await run(() => postApi.list(params), '');
    if (data) setPosts(data.posts || []);
  }

  function handleFilterChange(key: string, value: string) {
    const newFilters = { ...filters, [key]: value };
    setFilters(newFilters);
    loadPosts();
  }

  return (
    <div className="home-page">
      {/* 简化筛选栏 */}
      <div className="card compact-filter">
        <div className="toolbar compact">
          <select className="field small" value={filters.sort} onChange={(e) => handleFilterChange('sort', e.target.value)}>
            <option value="latest">最新</option>
            <option value="hot">热门</option>
            <option value="recommend">推荐</option>
          </select>
          <select className="field small" value={filters.tagId} onChange={(e) => handleFilterChange('tagId', e.target.value)}>
            <option value="">全部标签</option>
            {tags.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
          </select>
        </div>
      </div>

      {/* 帖子列表（紧凑） */}
      <div className="card">
        <div className="post-list-compact">
          {posts.map((post) => (
            <PostListItem
              key={post.id}
              post={post}
              onClick={() => navigate(`/post/${post.id}`)}
            />
          ))}
        </div>
        {posts.length === 0 && <p className="muted">暂无帖子，切换筛选条件试试。</p>}
      </div>
    </div>
  );
}
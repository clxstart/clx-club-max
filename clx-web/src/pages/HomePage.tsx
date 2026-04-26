import { FormEvent, useState, useEffect } from 'react';
import { RefreshCcw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { postApi } from '../api';
import type { CategoryVO, PostListItemVO, TagVO } from '../api/types';

interface HomePageProps {
  categories: CategoryVO[];
  tags: TagVO[];
  run: <T>(action: () => Promise<T>, ok: string) => Promise<T | undefined>;
}

// 首页：帖子列表（内容流）
export function HomePage({ categories, tags, run }: HomePageProps) {
  const navigate = useNavigate();
  const [posts, setPosts] = useState<PostListItemVO[]>([]);
  const [filters, setFilters] = useState({ page: 1, size: 10, sort: 'latest', categoryId: '', tagId: '' });

  useEffect(() => {
    loadPosts();
  }, []);

  async function loadPosts(nextFilters = filters) {
    const data = await run(() => postApi.list(nextFilters), '帖子列表已刷新。');
    if (data) setPosts(data.posts || []);
  }

  function labelDate(value?: string) {
    if (!value) return '刚刚';
    return value.replace('T', ' ').slice(0, 16);
  }

  function handleRefresh() {
    loadPosts(filters);
  }

  function handleFilterChange(key: string, value: string) {
    const newFilters = { ...filters, [key]: value };
    setFilters(newFilters);
    loadPosts(newFilters);
  }

  return (
    <div className="home-page">
      {/* Hero 区域 */}
      <div className="card hero">
        <h2>发现新帖子，参与真实讨论。</h2>
        <p>你可以按分类和热度浏览内容，点击帖子查看详情、评论和点赞。</p>
        <div className="toolbar">
          <select className="field" value={filters.sort} onChange={(e) => handleFilterChange('sort', e.target.value)}>
            <option value="latest">最新</option>
            <option value="hot">热门</option>
            <option value="recommend">推荐</option>
          </select>
          <select className="field" value={filters.categoryId} onChange={(e) => handleFilterChange('categoryId', e.target.value)}>
            <option value="">全部分类</option>
            {categories.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
          </select>
          <select className="field" value={filters.tagId} onChange={(e) => handleFilterChange('tagId', e.target.value)}>
            <option value="">全部标签</option>
            {tags.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
          </select>
          <button className="soft-btn primary" onClick={handleRefresh}>
            <RefreshCcw size={16} /> 刷新列表
          </button>
        </div>
      </div>

      {/* 帖子列表 */}
      <div className="card">
        <h2>帖子列表</h2>
        <div className="post-list">
          {posts.map((post) => (
            <button
              className="post-item"
              key={post.id}
              onClick={() => navigate(`/post/${post.id}`)}
            >
              <h3>{post.title}</h3>
              <p className="muted">{post.summary || '暂无摘要，点击查看详情内容。'}</p>
              <div className="tags">
                {post.tags?.map((tag) => <span className="tag" key={tag.id}>{tag.name}</span>)}
              </div>
              <div className="post-meta">
                <span>{post.author?.name || '匿名用户'}</span>
                <span>{post.category?.name || '未分类'}</span>
                <span>赞 {post.likeCount || 0}</span>
                <span>评 {post.commentCount || 0}</span>
                <span>{labelDate(post.createdAt)}</span>
              </div>
            </button>
          ))}
        </div>
        {posts.length === 0 && <p className="muted">暂无帖子，点击刷新或切换筛选条件。</p>}
      </div>
    </div>
  );
}
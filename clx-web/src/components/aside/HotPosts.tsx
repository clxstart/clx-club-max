import type { PostListItemVO } from '../../api/types';

interface HotPostsProps {
  posts: PostListItemVO[];
}

// 热门文章组件
export function HotPosts({ posts }: HotPostsProps) {
  return (
    <div className="aside-card">
      <h3>热门文章</h3>
      <div className="aside-list">
        {posts.length === 0 && <p className="muted">暂无热门文章</p>}
        {posts.map((post, index) => (
          <div className="aside-list-item" key={post.id}>
            <span className={`rank ${index < 3 ? 'top3' : ''}`}>{index + 1}</span>
            <span className="name">{post.title}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

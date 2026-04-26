import type { PostListItemVO } from '../../api/types';

interface PostListItemProps {
  post: PostListItemVO;
  onClick: () => void;
}

// 紧凑帖子项组件（技术派风格）
// 一行显示：标签 + 作者 + 时间
// 标题单独一行
export function PostListItem({ post, onClick }: PostListItemProps) {
  // 格式化时间
  function formatTime(value?: string) {
    if (!value) return '刚刚';
    return value.replace('T', ' ').slice(0, 16);
  }

  // 获取第一个标签名
  const firstTag = post.tags?.[0];

  return (
    <button className="post-item-compact" onClick={onClick}>
      {/* 标签 + 作者 + 时间 */}
      <div className="post-tags-line">
        {firstTag && <span className="post-tag-mini">{firstTag.name}</span>}
        <span>{post.author?.name || '匿名'}</span>
        <span>·</span>
        <span>{formatTime(post.createdAt)}</span>
      </div>

      {/* 标题 */}
      <h4 className="post-title-line">{post.title}</h4>

      {/* 统计 */}
      <div className="post-stats-line">
        <span>赞 {post.likeCount || 0}</span>
        <span>评 {post.commentCount || 0}</span>
      </div>
    </button>
  );
}
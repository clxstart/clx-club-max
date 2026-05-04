import { useState, useEffect } from 'react';
import { Heart, ArrowLeft } from 'lucide-react';
import { useParams, useNavigate } from 'react-router-dom';
import { postApi } from '../api';
import type { PostDetailVO } from '../api/types';
import { CommentList } from '../components/comment/CommentList';
import { CommentInput } from '../components/comment/CommentInput';

interface PostDetailPageProps {
  run: <T>(action: () => Promise<T>, ok: string) => Promise<T | undefined>;
}

// 帖子详情页
export function PostDetailPage({ run }: PostDetailPageProps) {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [post, setPost] = useState<PostDetailVO>();
  const [loading, setLoading] = useState(true);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    if (id) {
      loadDetail(Number(id));
    }
  }, [id]);

  async function loadDetail(postId: number) {
    setLoading(true);
    const data = await run(() => postApi.detail(postId), '');
    if (data) {
      setPost(data);
    }
    setLoading(false);
  }

  async function handleLike() {
    if (!post) return;
    await run(
      () => post.isLiked ? postApi.unlikePost(post.id) : postApi.likePost(post.id),
      '点赞状态已更新。'
    );
    await loadDetail(post.id);
  }

  function formatTime(value?: string): string {
    if (!value) return '刚刚';
    return value.replace('T', ' ').slice(0, 16);
  }

  if (loading) {
    return (
      <div className="card">
        <p className="muted">正在加载帖子详情...</p>
      </div>
    );
  }

  if (!post) {
    return (
      <div className="card">
        <h2>帖子不存在</h2>
        <p className="muted">该帖子可能已被删除或不存在。</p>
        <button className="soft-btn primary" onClick={() => navigate('/')}>返回首页</button>
      </div>
    );
  }

  return (
    <div className="post-detail-page">
      {/* 帖子内容 */}
      <div className="card">
        <button className="back-btn" onClick={() => navigate(-1)}>
          <ArrowLeft size={16} />
          返回
        </button>

        <h1 className="post-title">{post.title}</h1>

        <div className="post-meta">
          <span className="author">{post.author?.name || '匿名用户'}</span>
          <span className="dot">·</span>
          <span>{formatTime(post.createdAt)}</span>
          <span className="dot">·</span>
          <span>浏览 {post.viewCount || 0}</span>
        </div>

        {post.tags && post.tags.length > 0 && (
          <div className="tags">
            {post.tags.map((tag) => (
              <span className="tag" key={tag.id}>{tag.name}</span>
            ))}
          </div>
        )}

        <div className="detail-content">{post.content}</div>

        <div className="post-actions">
          <button
            className={`action-btn ${post.isLiked ? 'liked' : ''}`}
            onClick={handleLike}
          >
            <Heart size={18} fill={post.isLiked ? 'currentColor' : 'none'} />
            <span>{post.likeCount || 0}</span>
          </button>
        </div>
      </div>

      {/* 评论输入 */}
      <div className="card comment-section">
        <CommentInput
          postId={post.id}
          onSuccess={() => setRefreshKey(k => k + 1)}
        />
      </div>

      {/* 评论列表 */}
      <div className="card">
        <CommentList key={refreshKey} postId={post.id} />
      </div>
    </div>
  );
}
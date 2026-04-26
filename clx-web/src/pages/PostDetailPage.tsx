import { FormEvent, useState, useEffect } from 'react';
import { Heart, MessageCircle } from 'lucide-react';
import { useParams, useNavigate } from 'react-router-dom';
import { postApi } from '../api';
import type { PostDetailVO, CommentVO } from '../api/types';

interface PostDetailPageProps {
  run: <T>(action: () => Promise<T>, ok: string) => Promise<T | undefined>;
}

// 帖子详情页：独立全屏展示
export function PostDetailPage({ run }: PostDetailPageProps) {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [post, setPost] = useState<PostDetailVO>();
  const [comments, setComments] = useState<CommentVO[]>([]);
  const [commentText, setCommentText] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      loadDetail(Number(id));
    }
  }, [id]);

  async function loadDetail(postId: number) {
    setLoading(true);
    const data = await run(() => postApi.detail(postId), '帖子详情已加载。');
    if (data) {
      setPost(data);
      const list = await postApi.comments(postId).catch(() => []);
      setComments(list);
    }
    setLoading(false);
  }

  async function handleComment(event: FormEvent) {
    event.preventDefault();
    if (!post) return;
    await run(() => postApi.addComment(post.id, { content: commentText }), '评论发布成功。');
    setCommentText('');
    const list = await postApi.comments(post.id).catch(() => []);
    setComments(list);
  }

  async function handleLike() {
    if (!post) return;
    await run(() => post.isLiked ? postApi.unlikePost(post.id) : postApi.likePost(post.id), '点赞状态已更新。');
    await loadDetail(post.id);
  }

  function labelDate(value?: string) {
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
      <div className="card">
        <button className="soft-btn" onClick={() => navigate('/')} style={{ marginBottom: 16 }}>
          返回列表
        </button>
        <h2>{post.title}</h2>
        <div className="post-meta">
          <span>{post.author?.name || '匿名用户'}</span>
          <span>浏览 {post.viewCount || 0}</span>
          <span>赞 {post.likeCount || 0}</span>
          <span>评 {post.commentCount || 0}</span>
          <span>{labelDate(post.createdAt)}</span>
        </div>
        <div className="tags">
          {post.tags?.map((tag) => <span className="tag" key={tag.id}>{tag.name}</span>)}
        </div>
        <p className="detail-content">{post.content}</p>
        <div className="toolbar">
          <button className="soft-btn primary" onClick={handleLike}>
            <Heart size={16} /> {post.isLiked ? '取消点赞' : '点赞'}
          </button>
        </div>
      </div>

      {/* 评论区 */}
      <div className="card">
        <h3><MessageCircle size={18} /> 评论 ({comments.length})</h3>
        <form className="form-grid" onSubmit={handleComment}>
          <textarea
            className="field"
            placeholder="写点真实想法..."
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
          />
          <button className="soft-btn primary">发表评论</button>
        </form>
        {comments.map((comment) => (
          <div className="comment" key={comment.id}>
            <strong>{comment.author?.name || '匿名用户'}</strong>
            <p>{comment.content}</p>
            <span className="muted">赞 {comment.likeCount || 0} · {labelDate(comment.createdAt)}</span>
          </div>
        ))}
        {comments.length === 0 && <p className="muted">暂无评论，快来发表第一条吧！</p>}
      </div>
    </div>
  );
}
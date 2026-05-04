import { useState, useEffect } from 'react';
import { MessageCircle, ThumbsUp, Reply } from 'lucide-react';
import { postApi } from '../../api';
import type { CommentVO } from '../../api/types';

interface CommentListProps {
  postId: number;
}

export function CommentList({ postId }: CommentListProps) {
  const [comments, setComments] = useState<CommentVO[]>([]);
  const [loading, setLoading] = useState(true);

  async function loadComments() {
    setLoading(true);
    try {
      const data = await postApi.comments(postId);
      setComments(data || []);
    } catch {
      setComments([]);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadComments();
  }, [postId]);

  async function handleLike(commentId: number) {
    try {
      await postApi.likeComment(commentId);
      loadComments();
    } catch (err) {
      console.error('点赞失败', err);
    }
  }

  if (loading) {
    return <div className="comment-loading">加载评论中...</div>;
  }

  if (comments.length === 0) {
    return <div className="comment-empty">暂无评论，快来发表第一条评论吧！</div>;
  }

  return (
    <div className="comment-list">
      <h3 className="comment-title">
        <MessageCircle size={18} />
        评论 {comments.length}
      </h3>
      {comments.map((comment) => (
        <CommentItem
          key={comment.id}
          comment={comment}
          onLike={handleLike}
          onReply={() => {}}
        />
      ))}
    </div>
  );
}

interface CommentItemProps {
  comment: CommentVO;
  onLike: (id: number) => void;
  onReply: (id: number) => void;
  depth?: number;
}

function CommentItem({ comment, onLike, onReply, depth = 0 }: CommentItemProps) {
  const [showReplies, setShowReplies] = useState(true);

  return (
    <div className={`comment-item ${depth > 0 ? 'reply' : ''}`}>
      <div className="comment-avatar">
        {comment.author?.avatar ? (
          <img src={comment.author.avatar} alt="" />
        ) : (
          <div className="avatar-placeholder">
            {comment.author?.name?.charAt(0) || '?'}
          </div>
        )}
      </div>
      <div className="comment-body">
        <div className="comment-header">
          <span className="comment-author">{comment.author?.name || '匿名用户'}</span>
          <span className="comment-time">{formatTime(comment.createdAt)}</span>
        </div>
        <div className="comment-content">{comment.content}</div>
        <div className="comment-actions">
          <button className="action-btn" onClick={() => onLike(comment.id)}>
            <ThumbsUp size={14} />
            {comment.likeCount || 0}
          </button>
          <button className="action-btn" onClick={() => onReply(comment.id)}>
            <Reply size={14} />
            回复
          </button>
        </div>
        {/* 子评论 */}
        {comment.children && comment.children.length > 0 && (
          <div className="comment-replies">
            <button
              className="toggle-replies"
              onClick={() => setShowReplies(!showReplies)}
            >
              {showReplies ? '收起' : `展开 ${comment.children.length} 条回复`}
            </button>
            {showReplies && comment.children.map((child) => (
              <CommentItem
                key={child.id}
                comment={child}
                onLike={onLike}
                onReply={onReply}
                depth={depth + 1}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

function formatTime(time?: string): string {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes} 分钟前`;
  if (hours < 24) return `${hours} 小时前`;
  if (days < 7) return `${days} 天前`;
  return time.substring(0, 10);
}

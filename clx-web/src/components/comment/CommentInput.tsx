import { useState } from 'react';
import { Send } from 'lucide-react';
import { postApi } from '../../api';

interface CommentInputProps {
  postId: number;
  parentId?: number;
  replyToId?: number;
  replyToName?: string;
  onSuccess?: () => void;
  onCancel?: () => void;
}

export function CommentInput({
  postId,
  parentId,
  replyToId,
  replyToName,
  onSuccess,
  onCancel
}: CommentInputProps) {
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit() {
    if (!content.trim()) return;

    setLoading(true);
    try {
      await postApi.addComment(postId, {
        content: content.trim(),
        parentId,
        replyToId
      });
      setContent('');
      onSuccess?.();
    } catch (err) {
      console.error('发表评论失败', err);
    } finally {
      setLoading(false);
    }
  }

  const isReply = !!parentId;

  return (
    <div className={`comment-input ${isReply ? 'reply-input' : ''}`}>
      {replyToName && (
        <div className="reply-to">
          回复 <span className="reply-name">@{replyToName}</span>
        </div>
      )}
      <textarea
        className="input-field"
        placeholder={isReply ? '写下你的回复...' : '写下你的评论...'}
        value={content}
        onChange={(e) => setContent(e.target.value)}
        rows={isReply ? 2 : 3}
      />
      <div className="input-actions">
        {isReply && onCancel && (
          <button className="cancel-btn" onClick={onCancel}>
            取消
          </button>
        )}
        <button
          className="submit-btn"
          onClick={handleSubmit}
          disabled={loading || !content.trim()}
        >
          <Send size={16} />
          {loading ? '发送中...' : '发送'}
        </button>
      </div>
    </div>
  );
}
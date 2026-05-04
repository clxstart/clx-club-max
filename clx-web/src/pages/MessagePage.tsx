import { useState, useEffect } from 'react';
import { MessageCircle, Bell, Send, User } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

type MessageTab = 'chat' | 'notification';

interface ChatSession {
  id: number;
  targetUserId: number;
  targetUserName: string;
  targetUserAvatar?: string;
  lastMessage: string;
  lastTime: string;
  unreadCount: number;
}

interface ChatMessage {
  id: number;
  sessionId: number;
  senderId: number;
  content: string;
  createdAt: string;
  isSelf: boolean;
}

interface Notification {
  id: number;
  type: string;
  title: string;
  content: string;
  isRead: boolean;
  createdAt: string;
  targetId?: number;
}

export function MessagePage() {
  const navigate = useNavigate();
  const [tab, setTab] = useState<MessageTab>('chat');
  const [sessions, setSessions] = useState<ChatSession[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [currentSession, setCurrentSession] = useState<ChatSession | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [newMessage, setNewMessage] = useState('');

  // 模拟数据
  useEffect(() => {
    setSessions([
      { id: 1, targetUserId: 10, targetUserName: '张三', lastMessage: '你好，最近怎么样？', lastTime: '2026-05-04 10:30', unreadCount: 2 },
      { id: 2, targetUserId: 11, targetUserName: '李四', lastMessage: '帖子写得不错！', lastTime: '2026-05-03 18:20', unreadCount: 0 },
    ]);
    setNotifications([
      { id: 1, type: 'comment', title: '新评论', content: '张三 评论了你的帖子「Java并发编程实战」', isRead: false, createdAt: '2026-05-04 09:15', targetId: 101 },
      { id: 2, type: 'like', title: '新点赞', content: '李四 赞了你的帖子', isRead: true, createdAt: '2026-05-03 20:30' },
      { id: 3, type: 'follow', title: '新关注', content: '王五 关注了你', isRead: false, createdAt: '2026-05-02 14:00' },
    ]);
  }, []);

  function handleSelectSession(session: ChatSession) {
    setCurrentSession(session);
    // 模拟加载消息
    setMessages([
      { id: 1, sessionId: session.id, senderId: session.targetUserId, content: '你好！', createdAt: '2026-05-04 10:00', isSelf: false },
      { id: 2, sessionId: session.id, senderId: 0, content: '你好，最近怎么样？', createdAt: '2026-05-04 10:05', isSelf: true },
      { id: 3, sessionId: session.id, senderId: session.targetUserId, content: session.lastMessage, createdAt: session.lastTime, isSelf: false },
    ]);
  }

  function handleSendMessage() {
    if (!newMessage.trim() || !currentSession) return;

    const msg: ChatMessage = {
      id: messages.length + 1,
      sessionId: currentSession.id,
      senderId: 0,
      content: newMessage.trim(),
      createdAt: new Date().toISOString(),
      isSelf: true
    };
    setMessages([...messages, msg]);
    setNewMessage('');
  }

  return (
    <div className="message-page">
      {/* Tab 切换 */}
      <div className="message-tabs">
        <button
          className={`tab-btn ${tab === 'chat' ? 'active' : ''}`}
          onClick={() => setTab('chat')}
        >
          <MessageCircle size={18} />
          私信
          {sessions.some(s => s.unreadCount > 0) && (
            <span className="unread-dot" />
          )}
        </button>
        <button
          className={`tab-btn ${tab === 'notification' ? 'active' : ''}`}
          onClick={() => setTab('notification')}
        >
          <Bell size={18} />
          通知
          {notifications.some(n => !n.isRead) && (
            <span className="unread-dot" />
          )}
        </button>
      </div>

      {/* 私信列表 */}
      {tab === 'chat' && (
        <div className="chat-container">
          {/* 会话列表 */}
          <div className="session-list">
            {sessions.map((session) => (
              <div
                key={session.id}
                className={`session-item ${currentSession?.id === session.id ? 'active' : ''}`}
                onClick={() => handleSelectSession(session)}
              >
                <div className="session-avatar">
                  {session.targetUserAvatar ? (
                    <img src={session.targetUserAvatar} alt="" />
                  ) : (
                    <div className="avatar-placeholder">
                      <User size={20} />
                    </div>
                  )}
                  {session.unreadCount > 0 && (
                    <span className="unread-count">{session.unreadCount}</span>
                  )}
                </div>
                <div className="session-info">
                  <div className="session-name">{session.targetUserName}</div>
                  <div className="session-preview">{session.lastMessage}</div>
                </div>
                <div className="session-time">{session.lastTime.slice(11, 16)}</div>
              </div>
            ))}
            {sessions.length === 0 && (
              <div className="empty-tip">暂无私信</div>
            )}
          </div>

          {/* 消息窗口 */}
          {currentSession && (
            <div className="chat-window">
              <div className="chat-header">
                <div className="chat-target" onClick={() => navigate(`/user/${currentSession.targetUserId}`)}>
                  {currentSession.targetUserAvatar ? (
                    <img src={currentSession.targetUserAvatar} alt="" />
                  ) : (
                    <div className="avatar-placeholder small">
                      <User size={16} />
                    </div>
                  )}
                  <span>{currentSession.targetUserName}</span>
                </div>
              </div>
              <div className="message-list">
                {messages.map((msg) => (
                  <div key={msg.id} className={`message-item ${msg.isSelf ? 'self' : 'other'}`}>
                    <div className="message-bubble">{msg.content}</div>
                    <div className="message-time">{msg.createdAt.slice(11, 16)}</div>
                  </div>
                ))}
              </div>
              <div className="message-input">
                <textarea
                  placeholder="输入消息..."
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                      e.preventDefault();
                      handleSendMessage();
                    }
                  }}
                />
                <button className="send-btn" onClick={handleSendMessage}>
                  <Send size={18} />
                </button>
              </div>
            </div>
          )}

          {!currentSession && sessions.length > 0 && (
            <div className="chat-placeholder">
              <MessageCircle size={48} />
              <p>选择一个对话开始聊天</p>
            </div>
          )}
        </div>
      )}

      {/* 通知列表 */}
      {tab === 'notification' && (
        <div className="notification-list">
          {notifications.map((notif) => (
            <div key={notif.id} className={`notification-item ${notif.isRead ? 'read' : 'unread'}`}>
              <div className="notif-icon">
                {notif.type === 'comment' && <MessageCircle size={20} />}
                {notif.type === 'like' && <span>❤️</span>}
                {notif.type === 'follow' && <User size={20} />}
              </div>
              <div className="notif-content">
                <div className="notif-title">{notif.title}</div>
                <div className="notif-text">{notif.content}</div>
                <div className="notif-time">{notif.createdAt}</div>
              </div>
              {!notif.isRead && <span className="unread-dot large" />}
            </div>
          ))}
          {notifications.length === 0 && (
            <div className="empty-tip">暂无通知</div>
          )}
        </div>
      )}
    </div>
  );
}
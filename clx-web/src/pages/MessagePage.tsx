import { useState, useEffect, useRef } from 'react';
import { MessageCircle, Bell, Send, User, Loader2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { messageApi } from '../api';
import type { ChatSessionVO, ChatMessageVO, NotificationVO, UnreadCountVO } from '../api/types';

type MessageTab = 'chat' | 'notification';

export function MessagePage() {
  const navigate = useNavigate();
  const [tab, setTab] = useState<MessageTab>('chat');
  const [sessions, setSessions] = useState<ChatSessionVO[]>([]);
  const [notifications, setNotifications] = useState<NotificationVO[]>([]);
  const [currentSession, setCurrentSession] = useState<ChatSessionVO | null>(null);
  const [messages, setMessages] = useState<ChatMessageVO[]>([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const [unreadCount, setUnreadCount] = useState<UnreadCountVO>({});
  const messageListRef = useRef<HTMLDivElement>(null);

  // 加载会话列表
  useEffect(() => {
    if (tab === 'chat') {
      loadSessions();
    } else {
      loadNotifications();
    }
    loadUnreadCount();
  }, [tab]);

  // 加载会话列表
  async function loadSessions() {
    setLoading(true);
    try {
      const result = await messageApi.sessions();
      setSessions(result.list || []);
    } catch (e) {
      console.error('加载会话失败', e);
    } finally {
      setLoading(false);
    }
  }

  // 加载通知列表
  async function loadNotifications() {
    setLoading(true);
    try {
      const result = await messageApi.notifications();
      setNotifications(result.list || []);
    } catch (e) {
      console.error('加载通知失败', e);
    } finally {
      setLoading(false);
    }
  }

  // 加载未读数
  async function loadUnreadCount() {
    try {
      const result = await messageApi.unreadCount();
      setUnreadCount(result);
    } catch (e) {
      console.error('加载未读数失败', e);
    }
  }

  // 选择会话，加载消息
  async function handleSelectSession(session: ChatSessionVO) {
    setCurrentSession(session);
    setLoading(true);
    try {
      const result = await messageApi.messages(session.sessionId);
      // 标记消息方向
      const userId = localStorage.getItem('clx_user_id');
      const list = (result.list || []).map((msg: ChatMessageVO) => ({
        ...msg,
        isSelf: msg.direction === 'sent' || msg.fromUserId === Number(userId)
      }));
      setMessages(list);
      // 标记已读
      if (session.unreadCount > 0) {
        await messageApi.markRead(session.sessionId);
        setSessions(prev => prev.map(s =>
          s.sessionId === session.sessionId ? { ...s, unreadCount: 0 } : s
        ));
      }
    } catch (e) {
      console.error('加载消息失败', e);
    } finally {
      setLoading(false);
    }
  }

  // 发送消息
  async function handleSendMessage() {
    if (!newMessage.trim() || !currentSession || sending) return;

    setSending(true);
    try {
      const result = await messageApi.send({
        toUserId: currentSession.targetUserId,
        content: newMessage.trim()
      });

      // 添加新消息到列表
      const msg: ChatMessageVO = {
        messageId: result.messageId,
        fromUserId: 0,
        content: newMessage.trim(),
        timestamp: result.timestamp,
        direction: 'sent',
        isSelf: true
      };
      setMessages(prev => [...prev, msg]);
      setNewMessage('');

      // 更新会话列表的最后消息
      setSessions(prev => prev.map(s =>
        s.sessionId === currentSession.sessionId
          ? { ...s, lastMessage: newMessage.trim(), lastTime: result.timestamp }
          : s
      ));

      // 滚动到底部
      setTimeout(() => {
        if (messageListRef.current) {
          messageListRef.current.scrollTop = messageListRef.current.scrollHeight;
        }
      }, 50);
    } catch (e) {
      console.error('发送消息失败', e);
    } finally {
      setSending(false);
    }
  }

  // 标记通知已读
  async function handleMarkNotificationRead(notif: NotificationVO) {
    if (notif.isRead) return;
    try {
      await messageApi.markNotificationRead(notif.id);
      setNotifications(prev => prev.map(n =>
        n.id === notif.id ? { ...n, isRead: true } : n
      ));
    } catch (e) {
      console.error('标记已读失败', e);
    }
  }

  // 标记全部通知已读
  async function handleMarkAllRead() {
    try {
      await messageApi.markAllRead();
      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })));
      setUnreadCount({});
    } catch (e) {
      console.error('标记全部已读失败', e);
    }
  }

  // 格式化时间
  function formatTime(timestamp?: number) {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    return date.toTimeString().slice(0, 5);
  }

  // 计算总未读数
  const totalUnread = (unreadCount.chat || 0) + (unreadCount.comment || 0) +
                      (unreadCount.like || 0) + (unreadCount.follow || 0) +
                      (unreadCount.system || 0);

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
          {(unreadCount.chat || 0) > 0 && (
            <span className="unread-badge">{unreadCount.chat}</span>
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
            {loading && sessions.length === 0 && (
              <div className="loading-tip">
                <Loader2 className="spin" size={24} />
                加载中...
              </div>
            )}
            {sessions.map((session) => (
              <div
                key={session.sessionId}
                className={`session-item ${currentSession?.sessionId === session.sessionId ? 'active' : ''}`}
                onClick={() => handleSelectSession(session)}
              >
                <div className="session-avatar">
                  {session.targetAvatar ? (
                    <img src={session.targetAvatar} alt="" />
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
                  <div className="session-name">{session.targetNickname}</div>
                  <div className="session-preview">{session.lastMessage || '暂无消息'}</div>
                </div>
                <div className="session-time">{formatTime(session.lastTime)}</div>
              </div>
            ))}
            {sessions.length === 0 && !loading && (
              <div className="empty-tip">暂无私信</div>
            )}
          </div>

          {/* 消息窗口 */}
          {currentSession && (
            <div className="chat-window">
              <div className="chat-header">
                <div className="chat-target" onClick={() => navigate(`/user/${currentSession.targetUserId}`)}>
                  {currentSession.targetAvatar ? (
                    <img src={currentSession.targetAvatar} alt="" />
                  ) : (
                    <div className="avatar-placeholder small">
                      <User size={16} />
                    </div>
                  )}
                  <span>{currentSession.targetNickname}</span>
                </div>
              </div>
              <div className="message-list" ref={messageListRef}>
                {loading && messages.length === 0 && (
                  <div className="loading-tip">
                    <Loader2 className="spin" size={24} />
                    加载中...
                  </div>
                )}
                {messages.map((msg) => (
                  <div key={msg.messageId} className={`message-item ${msg.isSelf ? 'self' : 'other'}`}>
                    <div className="message-bubble">{msg.content}</div>
                    <div className="message-time">{formatTime(msg.timestamp)}</div>
                  </div>
                ))}
                {messages.length === 0 && !loading && (
                  <div className="empty-tip">暂无消息，发送第一条消息吧</div>
                )}
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
                  disabled={sending}
                />
                <button className="send-btn" onClick={handleSendMessage} disabled={sending || !newMessage.trim()}>
                  {sending ? <Loader2 className="spin" size={18} /> : <Send size={18} />}
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
        <div className="notification-container">
          {notifications.some(n => !n.isRead) && (
            <div className="notification-header">
              <button className="mark-all-btn" onClick={handleMarkAllRead}>
                全部标记已读
              </button>
            </div>
          )}
          <div className="notification-list">
            {loading && notifications.length === 0 && (
              <div className="loading-tip">
                <Loader2 className="spin" size={24} />
                加载中...
              </div>
            )}
            {notifications.map((notif) => (
              <div
                key={notif.id}
                className={`notification-item ${notif.isRead ? 'read' : 'unread'}`}
                onClick={() => handleMarkNotificationRead(notif)}
              >
                <div className="notif-icon">
                  {notif.type === 'comment' && <MessageCircle size={20} />}
                  {notif.type === 'like' && <span>❤️</span>}
                  {notif.type === 'follow' && <User size={20} />}
                  {notif.type === 'system' && <Bell size={20} />}
                </div>
                <div className="notif-content">
                  <div className="notif-title">{notif.title}</div>
                  <div className="notif-text">{notif.content}</div>
                  <div className="notif-time">{formatTime(notif.createTime)}</div>
                </div>
                {!notif.isRead && <span className="unread-dot large" />}
              </div>
            ))}
            {notifications.length === 0 && !loading && (
              <div className="empty-tip">暂无通知</div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
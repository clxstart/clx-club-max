import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { UserRound, Heart, Users, Edit, Bookmark, ChevronLeft, Mail } from 'lucide-react';
import { userApi, postApi, getStoredToken } from '../api';
import type { UserProfileVO, UserSimpleVO, PostListItemVO, ProfileUpdateRequest } from '../api/types';

type Tab = 'posts' | 'following' | 'fans' | 'favorites';

export function UserProfilePage() {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const [profile, setProfile] = useState<UserProfileVO>();
  const [posts, setPosts] = useState<PostListItemVO[]>([]);
  const [following, setFollowing] = useState<UserSimpleVO[]>([]);
  const [fans, setFans] = useState<UserSimpleVO[]>([]);
  const [favorites, setFavorites] = useState<{ postId: number; title: string }[]>([]);
  const [activeTab, setActiveTab] = useState<Tab>('posts');
  const [loading, setLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState<ProfileUpdateRequest>({});
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);

  // 检查当前用户
  useEffect(() => {
    if (getStoredToken()) {
      userApi.me().then((me) => {
        if (me) setCurrentUserId(me.userId);
      }).catch(() => undefined);
    }
  }, []);

  // 加载用户资料
  useEffect(() => {
    if (!userId) return;
    setLoading(true);
    userApi.profile(Number(userId))
      .then(setProfile)
      .catch(() => undefined)
      .finally(() => setLoading(false));
  }, [userId]);

  // 加载帖子列表
  useEffect(() => {
    if (!userId || activeTab !== 'posts') return;
    userApi.userPosts(Number(userId))
      .then((data) => setPosts(data.posts || []))
      .catch(() => undefined);
  }, [userId, activeTab]);

  // 加载关注列表
  useEffect(() => {
    if (!userId || activeTab !== 'following') return;
    userApi.following(Number(userId))
      .then((data) => setFollowing(data.list || []))
      .catch(() => undefined);
  }, [userId, activeTab]);

  // 加载粉丝列表
  useEffect(() => {
    if (!userId || activeTab !== 'fans') return;
    userApi.fans(Number(userId))
      .then((data) => setFans(data.list || []))
      .catch(() => undefined);
  }, [userId, activeTab]);

  // 加载收藏夹（仅自己）
  useEffect(() => {
    if (activeTab !== 'favorites' || !currentUserId || Number(userId) !== currentUserId) return;
    userApi.favorites()
      .then((data) => setFavorites(data.list || []))
      .catch(() => undefined);
  }, [userId, activeTab, currentUserId]);

  // 关注/取关
  async function handleFollow() {
    if (!profile || !currentUserId) return;
    setLoading(true);
    try {
      if (profile.isFollowed) {
        await userApi.unfollow(profile.userId);
        setProfile({ ...profile, isFollowed: false, fansCount: profile.fansCount! - 1 });
      } else {
        await userApi.follow(profile.userId);
        setProfile({ ...profile, isFollowed: true, fansCount: profile.fansCount! + 1 });
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  // 保存资料
  async function handleSaveEdit() {
    if (!currentUserId) return;
    setLoading(true);
    try {
      await userApi.updateProfile(editForm);
      const updated = await userApi.profile(currentUserId);
      setProfile(updated);
      setIsEditing(false);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }

  if (!profile && !loading) {
    return <div className="page-center"><p>用户不存在</p></div>;
  }

  const isOwnProfile = currentUserId === Number(userId);

  return <main className="profile-page">
    {/* 返回按钮 */}
    <button className="back-btn" onClick={() => navigate(-1)}>
      <ChevronLeft size={20} /> 返回
    </button>

    {/* 用户信息卡 */}
    <section className="profile-card">
      <div className="avatar-section">
        {profile?.avatar
          ? <img src={profile.avatar} alt="头像" className="avatar-img" />
          : <div className="avatar-placeholder"><UserRound size={48} /></div>
        }
        <h1 className="nickname">{profile?.nickname || profile?.username || '用户'}</h1>
        {profile?.signature && <p className="signature">{profile.signature}</p>}
      </div>

      {/* 三数统计 */}
      <div className="stats-row">
        <div className="stat-item" onClick={() => setActiveTab('following')}>
          <Users size={18} />
          <span className="stat-num">{profile?.followCount || 0}</span>
          <span className="stat-label">关注</span>
        </div>
        <div className="stat-item" onClick={() => setActiveTab('fans')}>
          <Users size={18} />
          <span className="stat-num">{profile?.fansCount || 0}</span>
          <span className="stat-label">粉丝</span>
        </div>
        <div className="stat-item">
          <Heart size={18} />
          <span className="stat-num">{profile?.likeTotalCount || 0}</span>
          <span className="stat-label">获赞</span>
        </div>
      </div>

      {/* 操作按钮 */}
      <div className="action-row">
        {isOwnProfile
          ? <>
              <button className="action-btn" onClick={() => setIsEditing(true)}>
                <Edit size={18} /> 编辑资料
              </button>
              <button className="action-btn" onClick={() => setActiveTab('favorites')}>
                <Bookmark size={18} /> 我的收藏
              </button>
            </>
          : <>
              <button className={`action-btn ${profile?.isFollowed ? 'followed' : 'primary'}`} onClick={handleFollow} disabled={loading}>
                {profile?.isFollowed ? '已关注' : '关注'}
              </button>
              <button className="action-btn" onClick={() => navigate('/compose')}>
                <Mail size={18} /> 私信
              </button>
            </>
        }
      </div>
    </section>

    {/* Tab 切换 */}
    <section className="tabs-row">
      <button className={activeTab === 'posts' ? 'active' : ''} onClick={() => setActiveTab('posts')}>帖子</button>
      <button className={activeTab === 'following' ? 'active' : ''} onClick={() => setActiveTab('following')}>关注</button>
      <button className={activeTab === 'fans' ? 'active' : ''} onClick={() => setActiveTab('fans')}>粉丝</button>
      {isOwnProfile && <button className={activeTab === 'favorites' ? 'active' : ''} onClick={() => setActiveTab('favorites')}>收藏</button>}
    </section>

    {/* 内容列表 */}
    <section className="content-list">
      {loading && <p className="loading-text">加载中...</p>}
      {activeTab === 'posts' && posts.map((post) => (
        <div className="post-item" key={post.id} onClick={() => navigate(`/post/${post.id}`)}>
          <h3>{post.title}</h3>
          <p className="post-meta">赞 {post.likeCount || 0} · 评 {post.commentCount || 0}</p>
        </div>
      ))}
      {activeTab === 'following' && following.map((user) => (
        <div className="user-item" key={user.userId} onClick={() => navigate(`/user/${user.userId}`)}>
          {user.avatar ? <img src={user.avatar} className="user-avatar" /> : <UserRound size={32} />}
          <div className="user-info">
            <span className="user-name">{user.nickname}</span>
            <span className="user-sig">{user.signature}</span>
          </div>
        </div>
      ))}
      {activeTab === 'fans' && fans.map((user) => (
        <div className="user-item" key={user.userId} onClick={() => navigate(`/user/${user.userId}`)}>
          {user.avatar ? <img src={user.avatar} className="user-avatar" /> : <UserRound size={32} />}
          <div className="user-info">
            <span className="user-name">{user.nickname}</span>
            <span className="user-sig">{user.signature}</span>
          </div>
        </div>
      ))}
      {activeTab === 'favorites' && favorites.map((item) => (
        <div className="post-item" key={item.postId} onClick={() => navigate(`/post/${item.postId}`)}>
          <h3>{item.title}</h3>
        </div>
      ))}
      {!loading && activeTab === 'posts' && posts.length === 0 && <p className="empty-text">暂无帖子</p>}
      {!loading && activeTab === 'following' && following.length === 0 && <p className="empty-text">暂无关注</p>}
      {!loading && activeTab === 'fans' && fans.length === 0 && <p className="empty-text">暂无粉丝</p>}
    </section>

    {/* 编辑弹窗 */}
    {isEditing && (
      <div className="modal-overlay">
        <div className="modal-card">
          <h2>编辑资料</h2>
          <div className="form-grid">
            <label>昵称</label>
            <input value={editForm.nickname || profile?.nickname || ''} onChange={(e) => setEditForm({ ...editForm, nickname: e.target.value })} />
            <label>签名</label>
            <textarea value={editForm.signature || profile?.signature || ''} onChange={(e) => setEditForm({ ...editForm, signature: e.target.value })} />
            <label>头像 URL</label>
            <input value={editForm.avatar || profile?.avatar || ''} onChange={(e) => setEditForm({ ...editForm, avatar: e.target.value })} />
          </div>
          <div className="modal-actions">
            <button className="action-btn" onClick={() => setIsEditing(false)}>取消</button>
            <button className="action-btn primary" onClick={handleSaveEdit} disabled={loading}>保存</button>
          </div>
        </div>
      </div>
    )}
  </main>;
}
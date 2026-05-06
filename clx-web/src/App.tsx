import { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import { Home, Search, BookOpen, UserRound, LogOut, Plus, MessageCircle } from 'lucide-react';
import { TopNavBar } from './components/layout/TopNavBar';
import { HomePage } from './pages/HomePage';
import { PostDetailPage } from './pages/PostDetailPage';
import { ComposePage } from './pages/ComposePage';
import { SearchPage } from './pages/SearchPage';
import { QuizPage } from './pages/QuizPage';
import { AccountPage } from './pages/AccountPage';
import { AuthPage } from './pages/AuthPage';
import { UserProfilePage } from './pages/UserProfilePage';
import { MessagePage } from './pages/MessagePage';
import { HotPosts } from './components/aside/HotPosts';
import { ActiveRank } from './components/aside/ActiveRank';
import { authApi, clearToken, getStoredToken, postApi, searchApi, taxonomyApi, userApi } from './api';
import type { CategoryVO, PostListItemVO, TagVO, UserInfoVO, ActiveUserVO } from './api/types';

type Message = { text: string; error?: boolean };
type NavTab = 'home' | 'search' | 'quiz' | 'compose' | 'message' | 'account';

// 主应用容器（带路由）
function AppContent() {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [tab, setTab] = useState<NavTab>('home');
  const [activeCategory, setActiveCategory] = useState('');
  const [message, setMessage] = useState<Message>({ text: '欢迎来到 CLXHXH 社区。' });
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState<UserInfoVO>();
  const [categories, setCategories] = useState<CategoryVO[]>([]);
  const [tags, setTags] = useState<TagVO[]>([]);
  const [hotPosts, setHotPosts] = useState<PostListItemVO[]>([]);
  const [hotKeywords, setHotKeywords] = useState<unknown[]>([]);
  const [activeUsers, setActiveUsers] = useState<ActiveUserVO[]>([]);
  const [bindings, setBindings] = useState<unknown[]>([]);

  // 通用执行器
  async function run<T>(action: () => Promise<T>, ok: string): Promise<T | undefined> {
    try {
      setLoading(true);
      const result = await action();
      if (ok) setMessage({ text: ok });
      return result;
    } catch (error) {
      setMessage({ text: error instanceof Error ? error.message : '操作失败', error: true });
      return undefined;
    } finally {
      setLoading(false);
    }
  }

  // 加载登录后数据
  async function loadAuthedData() {
    const [categoryList, tagList] = await Promise.all([taxonomyApi.categories(), taxonomyApi.tags()]);
    setCategories(categoryList || []);
    setTags(tagList || []);
    const hot = await postApi.hot(5).catch(() => []);
    setHotPosts(hot || []);
    const keywords = await searchApi.hot('day', 5).catch(() => []);
    setHotKeywords(keywords || []);
    // 调用真实活跃用户排行接口
    const active = await userApi.active(5).catch(() => []);
    setActiveUsers(active || []);
    const me = await authApi.me().catch(() => undefined);
    if (me) setUser(me);
  }

  // 处理 OAuth 回调
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const oauthToken = params.get('oauth_token');
    const errorParam = params.get('error');
    if (oauthToken) {
      window.history.replaceState({}, '', window.location.pathname);
      localStorage.setItem('clx_token', oauthToken);
      localStorage.setItem('clx_token_name', 'Authorization');
      setIsLoggedIn(true);
      setMessage({ text: 'GitHub 登录成功！' });
      loadAuthedData();
    } else if (errorParam) {
      window.history.replaceState({}, '', window.location.pathname);
      setMessage({ text: errorParam, error: true });
    }

    // 检查已有登录态
    if (getStoredToken() && !oauthToken) {
      setIsLoggedIn(true);
      loadAuthedData();
    }
  }, []);

  // 同步 tab 和路由
  useEffect(() => {
    const pathMap: Record<string, NavTab> = {
      '/': 'home',
      '/search': 'search',
      '/quiz': 'quiz',
      '/compose': 'compose',
      '/message': 'message',
      '/account': 'account'
    };
    const currentPath = location.pathname;
    if (pathMap[currentPath]) {
      setTab(pathMap[currentPath]);
    }
  }, [location.pathname]);

  // 登录成功回调
  function handleLoginSuccess() {
    setIsLoggedIn(true);
    loadAuthedData();
    navigate('/');
    setTab('home');
  }

  // Tab 切换
  function handleTabChange(newTab: NavTab) {
    setTab(newTab);
    const pathMap: Record<NavTab, string> = {
      home: '/',
      search: '/search',
      quiz: '/quiz',
      compose: '/compose',
      message: '/message',
      account: '/account'
    };
    navigate(pathMap[newTab]);
  }

  // 退出登录
  function handleLogout() {
    clearToken();
    setUser(undefined);
    setCategories([]);
    setTags([]);
    setHotPosts([]);
    setHotKeywords([]);
    setActiveUsers([]);
    setBindings([]);
    setIsLoggedIn(false);
    setMessage({ text: '已退出登录。' });
    navigate('/auth');
  }

  // 顶部工具栏导航项
  const navItems: { key: NavTab; label: string; icon: React.ReactNode }[] = [
    { key: 'home', label: '首页', icon: <Home size={18} /> },
    { key: 'search', label: '搜索', icon: <Search size={18} /> },
    { key: 'quiz', label: '刷题', icon: <BookOpen size={18} /> },
    { key: 'compose', label: '发帖', icon: <Plus size={18} /> },
    { key: 'message', label: '消息', icon: <MessageCircle size={18} /> },
    { key: 'account', label: '账号', icon: <UserRound size={18} /> },
  ];

  // 未登录：显示登录页
  if (!isLoggedIn) {
    return (
      <main className="app-shell auth-shell">
        <Routes>
          <Route path="/auth" element={<AuthPage run={run} setMessage={setMessage} onLoginSuccess={handleLoginSuccess} />} />
          <Route path="*" element={<Navigate to="/auth" replace />} />
        </Routes>
      </main>
    );
  }

  // 已登录：顶部导航布局
  return (
    <main className="shell-top-nav">
      {/* 顶部导航栏 */}
      <header className="top-header">
        <div className="top-brand">
          <div className="top-logo">CLX</div>
          <span className="top-title">CLXHXH</span>
        </div>
        <div className="top-nav-actions">
          {navItems.map((item) => (
            <button
              key={item.key}
              className={`top-action-btn ${tab === item.key ? 'active' : ''}`}
              onClick={() => handleTabChange(item.key)}
            >
              {item.icon}
              <span>{item.label}</span>
            </button>
          ))}
          <button className="top-action-btn logout" onClick={handleLogout}>
            <LogOut size={18} />
            <span>退出</span>
          </button>
        </div>
      </header>

      {/* 状态提示 */}
      <p className={`status ${message.error ? 'error' : ''}`}>
        {loading ? '正在加载… ' : ''}{message.text}
      </p>

      {/* 首页：顶部分类标签栏 */}
      {tab === 'home' && (
        <TopNavBar
          categories={categories}
          activeCategory={activeCategory}
          onCategoryChange={setActiveCategory}
        />
      )}

      {/* 主内容区 + 右侧栏 */}
      <div className="app-layout">
        <div className="main-area">
          <Routes>
            <Route path="/" element={<HomePage categories={categories} tags={tags} activeCategory={activeCategory} run={run} />} />
            <Route path="/post/:id" element={<PostDetailPage run={run} />} />
            <Route path="/user/:userId" element={<UserProfilePage />} />
            <Route path="/compose" element={<ComposePage categories={categories} tags={tags} run={run} onCreated={() => { setTab('home'); navigate('/'); }} />} />
            <Route path="/search" element={<SearchPage hotKeywords={hotKeywords} setHotKeywords={setHotKeywords} setMessage={setMessage} run={run} />} />
            <Route path="/quiz" element={<QuizPage run={run} setMessage={setMessage} />} />
            <Route path="/message" element={<MessagePage />} />
            <Route path="/account" element={<AccountPage user={user} bindings={bindings} setBindings={setBindings} run={run} setMessage={setMessage} onLogout={handleLogout} />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>

        {/* 右侧栏：首页显示热门和排行 */}
        {tab === 'home' && (
          <aside className="right-aside">
            <HotPosts posts={hotPosts} />
            <ActiveRank users={activeUsers} />
          </aside>
        )}
      </div>
    </main>
  );
}

// 主应用（包裹 BrowserRouter）
export function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}

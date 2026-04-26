import { useEffect, useMemo, useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { LeftNav, NavTab } from './components/layout/LeftNav';
import { HomePage } from './pages/HomePage';
import { PostDetailPage } from './pages/PostDetailPage';
import { ComposePage } from './pages/ComposePage';
import { SearchPage } from './pages/SearchPage';
import { QuizPage } from './pages/QuizPage';
import { AccountPage } from './pages/AccountPage';
import { AuthPage } from './pages/AuthPage';
import { ListCard } from './shared/ListCard';
import { authApi, clearToken, getStoredToken, postApi, saveToken, searchApi, taxonomyApi } from './api';
import type { CategoryVO, PostListItemVO, TagVO, UserInfoVO } from './api/types';

type Message = { text: string; error?: boolean };

function itemTitle(item: unknown) {
  if (item && typeof item === 'object') {
    const data = item as Record<string, unknown>;
    return String(data.title || data.name || data.username || data.content || JSON.stringify(data));
  }
  return String(item ?? '暂无标题');
}

// 主应用容器（带路由）
function AppContent() {
  const navigate = useNavigate();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [tab, setTab] = useState<NavTab>('home');
  const [message, setMessage] = useState<Message>({ text: '欢迎来到 CLXHXH 社区。' });
  const [loading, setLoading] = useState(false);
  const [user, setUser] = useState<UserInfoVO>();
  const [categories, setCategories] = useState<CategoryVO[]>([]);
  const [tags, setTags] = useState<TagVO[]>([]);
  const [hotPosts, setHotPosts] = useState<PostListItemVO[]>([]);
  const [hotKeywords, setHotKeywords] = useState<unknown[]>([]);
  const [bindings, setBindings] = useState<unknown[]>([]);

  // 通用执行器
  async function run<T>(action: () => Promise<T>, ok: string): Promise<T | undefined> {
    try {
      setLoading(true);
      const result = await action();
      setMessage({ text: ok });
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
    const hot = await postApi.hot(8).catch(() => []);
    setHotPosts(hot || []);
    const keywords = await searchApi.hot('day', 8).catch(() => []);
    setHotKeywords(keywords || []);
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
      '/account': 'account'
    };
    const currentPath = window.location.pathname;
    if (pathMap[currentPath]) {
      setTab(pathMap[currentPath]);
    }
  }, []);

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
    setBindings([]);
    setIsLoggedIn(false);
    setMessage({ text: '已退出登录。' });
    navigate('/auth');
  }

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

  // 已登录：显示三栏布局
  return (
    <main className="shell-with-nav">
      {/* 左侧导航 */}
      <LeftNav currentTab={tab} onTabChange={handleTabChange} onLogout={handleLogout} />

      {/* 状态提示 */}
      <p className={`status ${message.error ? 'error' : ''}`}>
        {loading ? '正在加载… ' : ''}{message.text}
      </p>

      {/* 主内容区 + 右侧栏 */}
      <div className="app-layout">
        <div className="main-area">
          <Routes>
            <Route path="/" element={<HomePage categories={categories} tags={tags} run={run} />} />
            <Route path="/post/:id" element={<PostDetailPage run={run} />} />
            <Route path="/compose" element={<ComposePage categories={categories} tags={tags} run={run} onCreated={() => { setTab('home'); navigate('/'); }} />} />
            <Route path="/search" element={<SearchPage hotKeywords={hotKeywords} setHotKeywords={setHotKeywords} setMessage={setMessage} run={run} />} />
            <Route path="/quiz" element={<QuizPage run={run} setMessage={setMessage} />} />
            <Route path="/account" element={<AccountPage user={user} bindings={bindings} setBindings={setBindings} run={run} setMessage={setMessage} onLogout={handleLogout} />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </div>

        {/* 右侧栏：首页显示热门 */}
        {tab === 'home' && (
          <aside className="right-aside">
            <ListCard title="热门帖子" items={hotPosts.map((item) => item.title)} />
            <ListCard title="热门搜索词" items={hotKeywords.map(itemTitle)} />
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
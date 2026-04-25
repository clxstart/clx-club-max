import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Globe2, Heart, Layers, LockKeyhole, LogOut, Mail, MessageCircle, RefreshCcw, Search, Send, ShieldCheck, Sparkles, UserRound, BookOpen, CheckCircle, XCircle } from 'lucide-react';
import { authApi, clearToken, getStoredToken, postApi, saveToken, searchApi, taxonomyApi, quizApi } from './api';
import type { CaptchaResult, CategoryVO, CommentVO, LoginVO, PostDetailVO, PostListItemVO, SearchVO, TagVO, UserInfoVO, SubjectCategoryVO, SubjectLabelVO, SubjectVO, PracticeSubjectVO, SubmitResultVO, PracticeResultVO, WrongBookVO } from './api/types';

type Tab = 'home' | 'auth' | 'compose' | 'search' | 'account' | 'quiz';
type Message = { text: string; error?: boolean };

const emptyLogin = { username: '', password: '', captchaCode: '', rememberMe: true };
const emptyRegister = { username: '', password: '', confirmPassword: '', nickname: '', email: '', emailCode: '' };

function captchaSrc(captcha?: CaptchaResult) {
  return captcha?.image || '';
}

function labelDate(value?: string) {
  if (!value) return '刚刚';
  return value.replace('T', ' ').slice(0, 16);
}

function itemTitle(item: unknown) {
  if (item && typeof item === 'object') {
    const data = item as Record<string, unknown>;
    return String(data.title || data.name || data.username || data.content || JSON.stringify(data));
  }
  return String(item ?? '暂无标题');
}

export function App() {
  const [tab, setTab] = useState<Tab>(() => getStoredToken() ? 'home' : 'auth');
  const [message, setMessage] = useState<Message>(() => getStoredToken()
    ? { text: '欢迎回来，社区内容已为你准备好。' }
    : { text: '请先登录或注册，登录后再进入社区。' });
  const [captcha, setCaptcha] = useState<CaptchaResult>();
  const [user, setUser] = useState<UserInfoVO>();
  const [categories, setCategories] = useState<CategoryVO[]>([]);
  const [tags, setTags] = useState<TagVO[]>([]);
  const [posts, setPosts] = useState<PostListItemVO[]>([]);
  const [hotPosts, setHotPosts] = useState<PostListItemVO[]>([]);
  const [selected, setSelected] = useState<PostDetailVO>();
  const [comments, setComments] = useState<CommentVO[]>([]);
  const [loading, setLoading] = useState(false);
  const [filters, setFilters] = useState({ page: 1, size: 10, sort: 'latest', categoryId: '', tagId: '' });
  const [login, setLogin] = useState(emptyLogin);
  const [register, setRegister] = useState(emptyRegister);
  const [phone, setPhone] = useState({ phone: '', smsCode: '', captchaCode: '' });
  const [reset, setReset] = useState({ email: '', captchaCode: '', resetCode: '', newPassword: '', confirmPassword: '' });
  const [compose, setCompose] = useState({ title: '', content: '', categoryId: '', tagIds: '' });
  const [commentText, setCommentText] = useState('');
  const [keyword, setKeyword] = useState('');
  const [searchResult, setSearchResult] = useState<SearchVO>();
  const [hotKeywords, setHotKeywords] = useState<unknown[]>([]);
  const [bindings, setBindings] = useState<unknown[]>([]);
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login');

  // Quiz states
  const [quizCategories, setQuizCategories] = useState<SubjectCategoryVO[]>([]);
  const [quizLabels, setQuizLabels] = useState<SubjectLabelVO[]>([]);
  const [quizSubjects, setQuizSubjects] = useState<SubjectVO[]>([]);
  const [quizTotal, setQuizTotal] = useState(0);
  const [quizFilters, setQuizFilters] = useState({ categoryId: '', labelId: '', keyword: '', pageNo: 1, pageSize: 10 });
  const [practiceId, setPracticeId] = useState<number>();
  const [practiceSubjectIds, setPracticeSubjectIds] = useState<number[]>([]);
  const [currentSubjectIndex, setCurrentSubjectIndex] = useState(0);
  const [currentSubject, setCurrentSubject] = useState<PracticeSubjectVO>();
  const [userAnswer, setUserAnswer] = useState('');
  const [submitResult, setSubmitResult] = useState<SubmitResultVO>();
  const [practiceResult, setPracticeResult] = useState<PracticeResultVO>();
  const [wrongBooks, setWrongBooks] = useState<WrongBookVO[]>([]);
  const [wrongBookTotal, setWrongBookTotal] = useState(0);

  async function loadAuthedData() {
    await loadTaxonomy();
    await loadPosts();
    await loadHot();
    searchApi.hot('day', 8).then(setHotKeywords).catch(() => undefined);
    await loadMe();
  }

  const isLoggedIn = Boolean(getStoredToken());
  const selectedTags = useMemo(() => compose.tagIds.split(',').map((id) => id.trim()).filter(Boolean), [compose.tagIds]);

  function switchTab(nextTab: Tab) {
    if (nextTab !== 'auth' && !getStoredToken()) {
      setTab('auth');
      setMessage({ text: '请先登录或注册，不登录不能进入主页。', error: true });
      return;
    }
    setTab(nextTab);
  }

  async function run<T>(action: () => Promise<T>, ok: string) {
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

  async function refreshCaptcha() {
    const data = await run(() => authApi.captcha(), '图形验证码已刷新。');
    if (data) setCaptcha(data);
  }

  async function loadTaxonomy() {
    const data = await run(async () => {
      const [categoryList, tagList] = await Promise.all([taxonomyApi.categories(), taxonomyApi.tags()]);
      return { categoryList, tagList };
    }, '分类和标签已同步。');
    if (data) {
      setCategories(data.categoryList || []);
      setTags(data.tagList || []);
    }
  }

  async function loadPosts(nextFilters = filters) {
    const data = await run(() => postApi.list(nextFilters), '帖子列表已刷新。');
    if (data) setPosts(data.posts || []);
  }

  async function loadHot() {
    const data = await run(() => postApi.hot(8), '热门帖子已刷新。');
    if (data) setHotPosts(data || []);
  }

  async function loadDetail(id: number) {
    const data = await run(() => postApi.detail(id), '帖子详情已打开。');
    if (data) {
      setSelected(data);
      const list = await run(() => postApi.comments(id), '评论已同步。');
      if (list) setComments(list);
    }
  }

  async function loadMe() {
    const data = await run(() => authApi.me(), '当前用户信息已同步。');
    if (data) setUser(data);
  }

  useEffect(() => {
    refreshCaptcha();
    if (getStoredToken()) {
      loadAuthedData();
    }
  }, []);

  async function handleLogin(event: FormEvent) {
    event.preventDefault();
    const data = await run(() => authApi.login({ ...login, captchaId: captcha?.id || '' }), '登录成功。');
    if (data) {
      saveToken(data);
      await loadAuthedData();
      switchTab('home');
    }
  }

  async function handleRegister(event: FormEvent) {
    event.preventDefault();
    const data = await run(() => authApi.register(register), '注册成功，已保存登录态。');
    if (data) {
      saveToken(data);
      await loadAuthedData();
      switchTab('home');
    }
  }

  async function handlePhoneLogin(event: FormEvent) {
    event.preventDefault();
    const data = await run(() => authApi.phoneLogin({ phone: phone.phone, smsCode: phone.smsCode }), '手机号登录成功。');
    if (data) {
      saveToken(data as LoginVO);
      await loadAuthedData();
      switchTab('home');
    }
  }

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    const id = await run(() => postApi.create({
      title: compose.title,
      content: compose.content,
      categoryId: compose.categoryId || undefined,
      tagIds: selectedTags
    }), '帖子发布成功。');
    if (id) {
      setCompose({ title: '', content: '', categoryId: '', tagIds: '' });
      await loadPosts();
      await loadDetail(id);
      switchTab('home');
    }
  }

  async function handleComment(event: FormEvent) {
    event.preventDefault();
    if (!selected) return;
    await run(() => postApi.addComment(selected.id, { content: commentText }), '评论发布成功。');
    setCommentText('');
    const list = await postApi.comments(selected.id).catch(() => []);
    setComments(list);
  }

  async function handleSearch(event: FormEvent) {
    event.preventDefault();
    const data = await run(() => searchApi.aggregate({ keyword, page: 1, size: 10, enableHighlight: true, enableSuggest: true }), '聚合搜索完成。');
    if (data) setSearchResult(data);
  }

  // Quiz helper functions
  function loadPracticeSubject(pid: number, sid: number) {
    const subject = quizSubjects.find(s => s.id === sid);
    run(() => quizApi.practiceSubject(pid, sid, subject?.subjectType || 1), '题目已加载。').then((data) => {
      if (data) {
        setCurrentSubject(data);
        setUserAnswer('');
        setSubmitResult(undefined);
      }
    });
  }

  function nextSubject() {
    if (!practiceId) return;
    const nextIndex = currentSubjectIndex + 1;
    if (nextIndex >= practiceSubjectIds.length) {
      run(() => quizApi.practiceFinish(practiceId), '练习已结束。').then((data) => data && setPracticeResult(data));
    } else {
      setCurrentSubjectIndex(nextIndex);
      loadPracticeSubject(practiceId, practiceSubjectIds[nextIndex]);
    }
  }

  // Load quiz categories and labels on mount
  useEffect(() => {
    if (isLoggedIn) {
      quizApi.categories().then(setQuizCategories).catch(() => undefined);
      quizApi.labels().then(setQuizLabels).catch(() => undefined);
    }
  }, [isLoggedIn]);

  return <main className={`app-shell ${!isLoggedIn ? 'auth-shell' : ''}`}>
    {isLoggedIn && <header className="topbar">
      <div className="brand">
        <div className="logo">CLX</div>
        <div>
          <h1>CLXHXH</h1>
          <p>分享日常、交流经验，也可以发现社区里的热门内容。</p>
        </div>
      </div>
      <nav className="nav">
        {(['home', 'search', 'quiz', 'compose', 'auth', 'account'] as Tab[]).map((key) => <button key={key} className={tab === key ? 'active' : ''} onClick={() => switchTab(key)}>{({ home: '首页', search: '搜索', quiz: '刷题', compose: '发帖', auth: '登录注册', account: '账号' })[key]}</button>)}
      </nav>
    </header>}

    {isLoggedIn && <p className={`status ${message.error ? 'error' : ''}`}>{loading ? '正在加载… ' : ''}{message.text}</p>}

    {tab === 'home' && isLoggedIn && <section className="layout">
      <div>
        <div className="card hero">
          <h2>发现新帖子，参与真实讨论。</h2>
          <p>你可以按分类和热度浏览内容，也可以进入帖子详情评论、点赞。</p>
          <div className="toolbar">
            <select className="field" value={filters.sort} onChange={(event) => setFilters({ ...filters, sort: event.target.value })}>
              <option value="latest">最新</option><option value="hot">热门</option><option value="recommend">推荐</option>
            </select>
            <select className="field" value={filters.categoryId} onChange={(event) => setFilters({ ...filters, categoryId: event.target.value })}>
              <option value="">全部分类</option>{categories.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
            </select>
            <select className="field" value={filters.tagId} onChange={(event) => setFilters({ ...filters, tagId: event.target.value })}>
              <option value="">全部标签</option>{tags.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
            </select>
            <button className="soft-btn primary" onClick={() => loadPosts(filters)}><RefreshCcw size={16} /> 刷新列表</button>
          </div>
        </div>
        <div className="card">
          <h2>帖子列表</h2>
          <div className="post-list">{posts.map((post) => <button className={`post-item ${selected?.id === post.id ? 'selected' : ''}`} key={post.id} onClick={() => loadDetail(post.id)}>
            <h3>{post.title}</h3><p className="muted">{post.summary || '暂无摘要，点击查看详情内容。'}</p>
            <div className="tags">{post.tags?.map((tag) => <span className="tag" key={tag.id}>{tag.name}</span>)}</div>
            <div className="post-meta"><span>{post.author?.name || '匿名用户'}</span><span>{post.category?.name || '未分类'}</span><span>赞 {post.likeCount || 0}</span><span>评 {post.commentCount || 0}</span><span>{labelDate(post.createdAt)}</span></div>
          </button>)}</div>
        </div>
      </div>
      <aside className="side-stack">
        <PostDetailCard selected={selected} comments={comments} commentText={commentText} setCommentText={setCommentText} handleComment={handleComment} onLike={async () => selected && run(() => selected.isLiked ? postApi.unlikePost(selected.id) : postApi.likePost(selected.id), '点赞状态已更新。').then(() => loadDetail(selected.id))} />
        <ListCard title="热门帖子" items={hotPosts.map((item) => item.title)} />
        <ListCard title="热门搜索词" items={hotKeywords.map(itemTitle)} />
      </aside>
    </section>}

    {tab === 'auth' && <section className="auth-page">
      <div className="auth-particle" />
      <div className="auth-particle" />
      <div className="auth-particle" />
      <div className="auth-particle" />
      <div className="auth-particle" />
      <div className="auth-particle" />
      <div className="auth-brand">
        <div className="auth-logo"><Layers size={38} /></div>
        <strong>CLXHXH</strong>
      </div>
      <div className="auth-card">
        {authMode === 'login' ? <>
          <form className="form-grid" onSubmit={handleLogin}>
            <label className="auth-field-label">用户名</label>
            <div className="auth-input"><UserRound size={20} /><input className="field" placeholder="请输入用户名" value={login.username} onChange={(e) => setLogin({ ...login, username: e.target.value })} /></div>
            <label className="auth-field-label">密码</label>
            <div className="auth-input"><LockKeyhole size={20} /><input className="field" placeholder="请输入密码" type="password" value={login.password} onChange={(e) => setLogin({ ...login, password: e.target.value })} /></div>
            <label className="auth-field-label">图形验证码</label>
            <div className="captcha-row">
              <div className="auth-input captcha-input"><ShieldCheck size={20} /><input className="field" placeholder="请输入验证码" value={login.captchaCode} onChange={(e) => setLogin({ ...login, captchaCode: e.target.value })} /></div>
              {captchaSrc(captcha) && <img className="captcha-img" alt="点击刷新验证码" src={captchaSrc(captcha)} onClick={refreshCaptcha} title="点击刷新" />}
            </div>
            <div className="auth-options"><label><input type="checkbox" checked={login.rememberMe} onChange={(e) => setLogin({ ...login, rememberMe: e.target.checked })} /> 记住我</label><span>关闭浏览器后失效</span></div>
            <button className="soft-btn primary">登录</button>
          </form>
          <div className="auth-divider"><span>其他登录方式</span></div>
          <div className="social-login"><button type="button"><Globe2 size={20} /></button><button type="button">G</button><button type="button">G</button></div>
          <p className="auth-switch">没有账号？<button type="button" onClick={() => setAuthMode('register')}>注册</button></p>
        </> : <>
          <form className="form-grid" onSubmit={handleRegister}>
            <label className="auth-field-label">用户名</label>
            <div className="auth-input"><UserRound size={20} /><input className="field" placeholder="请输入用户名" value={register.username} onChange={(e) => setRegister({ ...register, username: e.target.value })} /></div>
            <label className="auth-field-label">昵称</label>
            <input className="field" placeholder="请输入昵称" value={register.nickname} onChange={(e) => setRegister({ ...register, nickname: e.target.value })} />
            <label className="auth-field-label">邮箱</label>
            <div className="auth-input"><Mail size={20} /><input className="field" placeholder="请输入邮箱" value={register.email} onChange={(e) => setRegister({ ...register, email: e.target.value })} /></div>
            <button type="button" className="soft-btn" onClick={() => run(() => authApi.sendEmailCode(register.email), '邮箱验证码已发送。')}>发送邮箱验证码</button>
            <input className="field" placeholder="请输入邮箱验证码" value={register.emailCode} onChange={(e) => setRegister({ ...register, emailCode: e.target.value })} />
            <div className="auth-input"><LockKeyhole size={20} /><input className="field" placeholder="请输入密码" type="password" value={register.password} onChange={(e) => setRegister({ ...register, password: e.target.value })} /></div>
            <div className="auth-input"><LockKeyhole size={20} /><input className="field" placeholder="请确认密码" type="password" value={register.confirmPassword} onChange={(e) => setRegister({ ...register, confirmPassword: e.target.value })} /></div>
            <button className="soft-btn primary">注册</button>
          </form>
          <p className="auth-switch">已有账号？<button type="button" onClick={() => setAuthMode('login')}>登录</button></p>
        </>}
      </div>
    </section>}

    {tab === 'compose' && isLoggedIn && <section className="layout">
      <div className="card"><h2>发布帖子</h2><form className="form-grid" onSubmit={handleCreate}>
        <input className="field" placeholder="标题" value={compose.title} onChange={(e) => setCompose({ ...compose, title: e.target.value })} />
        <textarea className="field" placeholder="内容" value={compose.content} onChange={(e) => setCompose({ ...compose, content: e.target.value })} />
        <select className="field" value={compose.categoryId} onChange={(e) => setCompose({ ...compose, categoryId: e.target.value })}><option value="">选择分类</option>{categories.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select>
        <input className="field" placeholder="标签 ID，用英文逗号分隔" value={compose.tagIds} onChange={(e) => setCompose({ ...compose, tagIds: e.target.value })} />
        <button className="soft-btn primary"><Send size={16} /> 发布</button>
      </form></div>
      <div className="side-stack"><ListCard title="可用分类" items={categories.map((item) => `${item.id} · ${item.name}`)} /><ListCard title="可用标签" items={tags.map((item) => `${item.id} · ${item.name}`)} /></div>
    </section>}

    {tab === 'search' && isLoggedIn && <section className="layout">
      <div className="card"><h2>聚合搜索</h2><form className="toolbar" onSubmit={handleSearch}><input className="field" placeholder="输入关键词" value={keyword} onChange={(e) => setKeyword(e.target.value)} /><button className="soft-btn primary"><Search size={16} /> 搜索</button></form>
        <div className="search-results">{Object.entries(searchResult?.results || {}).map(([type, result]) => <div className="post-item" key={type}><h3>{type} · {result.total} 条</h3>{result.error && <p className="error">{result.error}</p>}{result.items?.slice(0, 5).map((item, index) => <p key={index}>{itemTitle(item)}</p>)}</div>)}</div>
      </div>
      <div className="card"><h2>单类搜索 / 建议</h2><div className="form-grid"><button className="soft-btn" onClick={() => keyword && run(() => searchApi.single('post', keyword), '单类搜索完成。').then((data) => data && setSearchResult({ keyword, results: { post: data } }))}>搜索帖子</button><button className="soft-btn" onClick={() => keyword && run(() => searchApi.suggest(keyword), '搜索建议已获取。').then((data) => data && setMessage({ text: `建议：${data.join('、') || '暂无'}` }))}>获取建议</button><button className="soft-btn" onClick={() => run(() => searchApi.hot('day', 10), '热词已刷新。').then((data) => data && setHotKeywords(data))}>刷新热词</button></div><ListCard title="热词" items={hotKeywords.map(itemTitle)} /></div>
    </section>}

    {tab === 'quiz' && isLoggedIn && <section className="layout">
      <div className="card"><h2><BookOpen size={20} /> 题库</h2>
        <div className="toolbar">
          <select className="field" value={quizFilters.categoryId} onChange={(e) => setQuizFilters({ ...quizFilters, categoryId: e.target.value })}>
            <option value="">全部分类</option>{quizCategories.map((item) => <option key={item.id} value={item.id}>{item.categoryName}</option>)}
          </select>
          <select className="field" value={quizFilters.labelId} onChange={(e) => setQuizFilters({ ...quizFilters, labelId: e.target.value })}>
            <option value="">全部标签</option>{quizLabels.map((item) => <option key={item.id} value={item.id}>{item.labelName}</option>)}
          </select>
          <input className="field" placeholder="关键词" value={quizFilters.keyword} onChange={(e) => setQuizFilters({ ...quizFilters, keyword: e.target.value })} />
          <button className="soft-btn primary" onClick={() => run(() => quizApi.subjectPage(quizFilters), '题目列表已刷新。').then((data) => { if (data) { setQuizSubjects(data.list); setQuizTotal(data.total); } })}>搜索</button>
        </div>
        <div className="post-list">{quizSubjects.map((item) => <div className="post-item" key={item.id}>
          <h3>{item.subjectName}</h3>
          <p className="muted">类型：{['', '单选', '多选', '判断', '简答'][item.subjectType]} | 难度：{item.subjectDifficult}</p>
          <div className="post-meta"><span>{item.categoryName || '未分类'}</span><span>{item.labelNames?.join('、') || '无标签'}</span></div>
        </div>)}</div>
        <p className="muted">共 {quizTotal} 道题目</p>
      </div>
      <div className="card"><h2>开始练习</h2>
        <div className="form-grid">
          <input className="field" placeholder="标签ID（逗号分隔）" value={quizFilters.labelId} readOnly />
          <input className="field" placeholder="题目数量" type="number" defaultValue={10} id="quizCount" />
          <button className="soft-btn primary" onClick={() => {
            const count = Number((document.getElementById('quizCount') as HTMLInputElement)?.value) || 10;
            const labelIds = quizFilters.labelId ? [Number(quizFilters.labelId)] : undefined;
            run(() => quizApi.practiceStart({ labelIds, count }), '练习已开始。').then((data) => {
              if (data) {
                setPracticeId(data.practiceId);
                setPracticeSubjectIds(data.subjectIds);
                setCurrentSubjectIndex(0);
                setCurrentSubject(undefined);
                setSubmitResult(undefined);
                setPracticeResult(undefined);
                if (data.subjectIds.length > 0) {
                  loadPracticeSubject(data.practiceId, data.subjectIds[0]);
                }
              }
            });
          }}>开始练习</button>
        </div>
      </div>
      <div className="side-stack">
        <div className="card"><h2>答题中</h2>
          {!practiceId ? <p className="muted">点击"开始练习"开始答题。</p> : practiceResult ? <>
            <h3>练习结果</h3>
            <p>正确率：{(practiceResult.correctRate * 100).toFixed(1)}%</p>
            <p>用时：{practiceResult.timeUsed}</p>
            <p>错题：{practiceResult.wrongSubjectIds?.length || 0} 道</p>
            <button className="soft-btn" onClick={() => { setPracticeId(undefined); setPracticeResult(undefined); }}>重新开始</button>
          </> : <>
            <p className="muted">第 {currentSubjectIndex + 1} / {practiceSubjectIds.length} 题</p>
            {currentSubject && <>
              <h3>{currentSubject.subjectName}</h3>
              <p className="muted">类型：{['', '单选', '多选', '判断', '简答'][currentSubject.subjectType]}</p>
              {currentSubject.optionList?.map((opt, i) => <div key={i}>
                <label><input type={currentSubject.subjectType === 2 ? 'checkbox' : 'radio'} name="answer" value={opt.optionType} onChange={() => setUserAnswer(String(opt.optionType))} /> {['A', 'B', 'C', 'D', 'E', 'F'][opt.optionType - 1]}. {opt.optionContent}</label>
              </div>)}
              {currentSubject.subjectType === 4 && <textarea className="field" placeholder="输入你的答案" value={userAnswer} onChange={(e) => setUserAnswer(e.target.value)} />}
              {submitResult && <>
                <p className={submitResult.isCorrect === 1 ? '' : 'error'}>{submitResult.isCorrect === 1 ? '✓ 正确' : submitResult.isCorrect === 0 ? '✗ 错误' : '需自评'}</p>
                <p><strong>正确答案：</strong>{submitResult.correctAnswer}</p>
                <p><strong>解析：</strong>{submitResult.subjectParse}</p>
                {submitResult.needSelfJudge && <div className="toolbar">
                  <button className="soft-btn primary" onClick={() => run(() => quizApi.practiceSelfJudge(practiceId!, currentSubject.subjectId, 1), '已标记为正确。').then(() => nextSubject())}>我答对了</button>
                  <button className="soft-btn danger" onClick={() => run(() => quizApi.practiceSelfJudge(practiceId!, currentSubject.subjectId, 0), '已标记为错误。').then(() => nextSubject())}>我答错了</button>
                </div>}
                {!submitResult.needSelfJudge && <button className="soft-btn primary" onClick={() => nextSubject()}>下一题</button>}
              </>}
              {!submitResult && <button className="soft-btn primary" onClick={() => {
                if (!userAnswer) { setMessage({ text: '请先选择答案', error: true }); return; }
                run(() => quizApi.practiceSubmit({ practiceId: practiceId!, subjectId: currentSubject.subjectId, subjectType: currentSubject.subjectType, answerContent: userAnswer }), '答案已提交。').then((data) => data && setSubmitResult(data));
              }}>提交答案</button>}
            </>}
          </>}
        </div>
        <div className="card"><h2>错题本</h2>
          <button className="soft-btn" onClick={() => run(() => quizApi.wrongBookList(1, 10), '错题本已加载。').then((data) => { if (data) { setWrongBooks(data.list); setWrongBookTotal(data.total); } })}>加载错题本</button>
          {wrongBooks.map((item, i) => <p className="muted" key={i}>{item.subjectName} (错{item.wrongCount}次)</p>)}
          <p className="muted">共 {wrongBookTotal} 道错题</p>
        </div>
      </div>
    </section>}

    {tab === 'account' && isLoggedIn && <section className="layout">
      <div className="card"><h2>账号中心</h2><div className="kpis"><div className="kpi"><strong>{user?.userId || '-'}</strong><span>用户 ID</span></div><div className="kpi"><strong>{user?.username || '未登录'}</strong><span>用户名</span></div><div className="kpi"><strong>{bindings.length}</strong><span>社交绑定</span></div></div><div className="toolbar"><button className="soft-btn primary" onClick={loadMe}><UserRound size={16} /> 刷新个人信息</button><button className="soft-btn" onClick={() => run(() => authApi.refresh(), '登录状态已刷新。').then((data) => data && saveToken(data))}>刷新登录状态</button><button className="soft-btn danger" onClick={() => run(() => authApi.logout(), '已退出登录。').then(() => { clearToken(); setUser(undefined); setPosts([]); setHotPosts([]); setSelected(undefined); setComments([]); setSearchResult(undefined); switchTab('auth'); })}><LogOut size={16} /> 退出</button></div></div>
      <div className="side-stack"><div className="card"><h2>社交绑定</h2><div className="form-grid"><button className="soft-btn" onClick={() => run(() => authApi.bindings(), '绑定列表已获取。').then((data) => data && setBindings(data))}>获取绑定列表</button><button className="soft-btn" onClick={() => run(() => authApi.oauthAuthorize('github'), 'GitHub 授权地址已获取。').then((url) => url && setMessage({ text: String(url) }))}>GitHub 登录授权</button><button className="soft-btn" onClick={() => run(() => authApi.bindAuthorize('github'), 'GitHub 绑定授权地址已获取。').then((url) => url && setMessage({ text: String(url) }))}>GitHub 绑定授权</button></div><ListCard title="绑定项" items={bindings.map(itemTitle)} /></div>
      <div className="card"><h2>密码重置</h2><div className="form-grid"><input className="field" placeholder="邮箱" value={reset.email} onChange={(e) => setReset({ ...reset, email: e.target.value })} /><input className="field" placeholder="图形验证码" value={reset.captchaCode} onChange={(e) => setReset({ ...reset, captchaCode: e.target.value })} /><button className="soft-btn" onClick={() => run(() => authApi.sendPasswordReset({ email: reset.email, captchaId: captcha?.id || '', captchaCode: reset.captchaCode }), '重置邮件已发送。')}>发送重置邮件</button><input className="field" placeholder="重置码" value={reset.resetCode} onChange={(e) => setReset({ ...reset, resetCode: e.target.value })} /><input className="field" placeholder="新密码" type="password" value={reset.newPassword} onChange={(e) => setReset({ ...reset, newPassword: e.target.value })} /><input className="field" placeholder="确认密码" type="password" value={reset.confirmPassword} onChange={(e) => setReset({ ...reset, confirmPassword: e.target.value })} /><button className="soft-btn primary" onClick={() => run(() => authApi.confirmPasswordReset(reset), '密码已重置。')}>确认重置</button></div></div></div>
    </section>}
  </main>;
}

function CaptchaBox({ captcha, onRefresh }: { captcha?: CaptchaResult; onRefresh: () => void }) {
  return <div className="captcha"><button type="button" className="soft-btn" onClick={onRefresh}><RefreshCcw size={16} /> 刷新验证码</button>{captchaSrc(captcha) && <img alt="验证码" src={captchaSrc(captcha)} />}</div>;
}

function ListCard({ title, items }: { title: string; items: string[] }) {
  return <div className="card"><h2>{title}</h2>{items.length ? items.map((item, index) => <p className="muted" key={`${item}-${index}`}>{item}</p>) : <p className="muted">暂无数据</p>}</div>;
}

function PostDetailCard({ selected, comments, commentText, setCommentText, handleComment, onLike }: { selected?: PostDetailVO; comments: CommentVO[]; commentText: string; setCommentText: (text: string) => void; handleComment: (event: FormEvent) => void; onLike: () => void }) {
  if (!selected) return <div className="card"><h2>帖子详情</h2><p className="muted">从左侧选择一个帖子，详情、评论和点赞状态会在这里展示。</p></div>;
  return <div className="card"><h2>{selected.title}</h2><div className="post-meta"><span>{selected.author?.name || '匿名用户'}</span><span>浏览 {selected.viewCount || 0}</span><span>赞 {selected.likeCount || 0}</span><span>{labelDate(selected.createdAt)}</span></div><p className="detail-content">{selected.content}</p><div className="toolbar"><button className="soft-btn primary" onClick={onLike}><Heart size={16} /> {selected.isLiked ? '取消点赞' : '点赞'}</button></div><h3><MessageCircle size={18} /> 评论</h3><form className="form-grid" onSubmit={handleComment}><textarea className="field" placeholder="写点真实想法…" value={commentText} onChange={(e) => setCommentText(e.target.value)} /><button className="soft-btn primary">发表评论</button></form>{comments.map((comment) => <div className="comment" key={comment.id}><strong>{comment.author?.name || '匿名用户'}</strong><p>{comment.content}</p><span className="muted">赞 {comment.likeCount || 0} · {labelDate(comment.createdAt)}</span></div>)}</div>;
}

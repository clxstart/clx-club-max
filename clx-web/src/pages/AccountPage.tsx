import { LogOut, UserRound } from 'lucide-react';
import { authApi, clearToken } from '../api';
import type { UserInfoVO } from '../api/types';

interface AccountPageProps {
  user?: UserInfoVO;
  bindings: unknown[];
  setBindings: (bindings: unknown[]) => void;
  run: <T>(action: () => Promise<T>, ok: string) => Promise<T | undefined>;
  setMessage: (msg: { text: string; error?: boolean }) => void;
  onLogout: () => void;
}

function itemTitle(item: unknown) {
  if (item && typeof item === 'object') {
    const data = item as Record<string, unknown>;
    return String(data.title || data.name || data.username || data.content || JSON.stringify(data));
  }
  return String(item ?? '暂无标题');
}

// 账号页
export function AccountPage({ user, bindings, setBindings, run, setMessage, onLogout }: AccountPageProps) {
  async function loadMe() {
    return run(() => authApi.me(), '用户信息已同步。');
  }

  async function handleLogout() {
    await run(() => authApi.logout(), '已退出登录。');
    onLogout();
  }

  return (
    <div className="account-page layout">
      <div className="card">
        <h2>账号中心</h2>
        <div className="kpis">
          <div className="kpi"><strong>{user?.userId || '-'}</strong><span>用户 ID</span></div>
          <div className="kpi"><strong>{user?.username || '未登录'}</strong><span>用户名</span></div>
          <div className="kpi"><strong>{bindings.length}</strong><span>社交绑定</span></div>
        </div>
        <div className="toolbar">
          <button className="soft-btn primary" onClick={loadMe}><UserRound size={16} /> 刷新个人信息</button>
          <button className="soft-btn" onClick={() => run(() => authApi.refresh(), '登录状态已刷新。').then((data) => data && authApi.saveToken(data))}>刷新登录状态</button>
          <button className="soft-btn danger" onClick={handleLogout}><LogOut size={16} /> 退出</button>
        </div>
      </div>

      <div className="side-stack">
        <div className="card">
          <h2>社交绑定</h2>
          <div className="form-grid">
            <button className="soft-btn" onClick={() => run(() => authApi.bindings(), '绑定列表已获取。').then((data) => data && setBindings(data))}>获取绑定列表</button>
            <button className="soft-btn" onClick={() => run(() => authApi.oauthAuthorize('github'), 'GitHub 授权地址已获取。').then((url) => url && setMessage({ text: String(url) }))}>GitHub 登录授权</button>
            <button className="soft-btn" onClick={() => run(() => authApi.bindAuthorize('github'), 'GitHub 绑定授权地址已获取。').then((url) => url && setMessage({ text: String(url) }))}>GitHub 绑定授权</button>
          </div>
          <div className="card">
            <h2>绑定项</h2>
            {bindings.map((item, i) => <p className="muted" key={i}>{itemTitle(item)}</p>)}
          </div>
        </div>
      </div>
    </div>
  );
}
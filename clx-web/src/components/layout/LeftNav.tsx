import type { ReactNode } from 'react';
import { BookOpen, Edit, Home, LogOut, Search, UserRound } from 'lucide-react';

export type NavTab = 'home' | 'search' | 'quiz' | 'compose' | 'account';

interface LeftNavProps {
  currentTab: NavTab;
  onTabChange: (tab: NavTab) => void;
  onLogout: () => void;
}

// 左侧垂直导航栏
// 桌面端固定宽度，移动端收起为底部导航
export function LeftNav({ currentTab, onTabChange, onLogout }: LeftNavProps) {
  const navItems: { key: NavTab; label: string; icon: ReactNode }[] = [
    { key: 'home', label: '首页', icon: <Home size={22} /> },
    { key: 'search', label: '搜索', icon: <Search size={22} /> },
    { key: 'quiz', label: '刷题', icon: <BookOpen size={22} /> },
    { key: 'compose', label: '发帖', icon: <Edit size={22} /> },
    { key: 'account', label: '账号', icon: <UserRound size={22} /> },
  ];

  return (
    <nav className="left-nav">
      <div className="nav-brand">
        <div className="nav-logo">CLX</div>
        <span className="nav-title">CLXHXH</span>
      </div>
      <div className="nav-items">
        {navItems.map((item) => (
          <button
            key={item.key}
            className={`nav-item ${currentTab === item.key ? 'active' : ''}`}
            onClick={() => onTabChange(item.key)}
            title={item.label}
          >
            {item.icon}
            <span className="nav-label">{item.label}</span>
          </button>
        ))}
      </div>
      <button className="nav-item logout" onClick={onLogout} title="退出登录">
        <LogOut size={22} />
        <span className="nav-label">退出</span>
      </button>
    </nav>
  );
}

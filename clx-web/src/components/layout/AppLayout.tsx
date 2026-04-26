import { ReactNode } from 'react';

interface AppLayoutProps {
  children: ReactNode;
  showRightAside?: boolean;
  rightContent?: ReactNode;
}

// 三栏布局容器：左导航 + 中内容 + 右辅助
// 桌面端三栏，平板两栏，移动端单栏
export function AppLayout({ children, showRightAside = false, rightContent }: AppLayoutProps) {
  return (
    <div className="app-layout">
      <main className="main-area">
        {children}
      </main>
      {showRightAside && rightContent && (
        <aside className="right-aside">
          {rightContent}
        </aside>
      )}
    </div>
  );
}

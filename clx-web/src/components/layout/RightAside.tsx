import { ReactNode } from 'react';

interface RightAsideProps {
  children?: ReactNode;
}

// 右侧辅助区域：热门帖子、热词等
// 桌面端显示，移动端隐藏
export function RightAside({ children }: RightAsideProps) {
  return (
    <aside className="right-aside">
      {children}
    </aside>
  );
}

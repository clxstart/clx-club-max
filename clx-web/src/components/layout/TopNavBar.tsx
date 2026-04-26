import { useRef, useEffect } from 'react';
import type { CategoryVO } from '../../api/types';

interface TopNavBarProps {
  categories: CategoryVO[];
  activeCategory: string;
  onCategoryChange: (id: string) => void;
}

// 顶部标签导航栏
// 水平滚动，选中态高亮
export function TopNavBar({ categories, activeCategory, onCategoryChange }: TopNavBarProps) {
  const scrollRef = useRef<HTMLDivElement>(null);

  // 移动端支持拖拽滚动
  useEffect(() => {
    const el = scrollRef.current;
    if (!el) return;

    let isDown = false;
    let startX = 0;
    let scrollLeft = 0;

    function onMouseDown(e: MouseEvent) {
      isDown = true;
      startX = e.pageX - el.offsetLeft;
      scrollLeft = el.scrollLeft;
    }

    function onMouseLeave() {
      isDown = false;
    }

    function onMouseUp() {
      isDown = false;
    }

    function onMouseMove(e: MouseEvent) {
      if (!isDown) return;
      e.preventDefault();
      const x = e.pageX - el.offsetLeft;
      const walk = (x - startX) * 1.5;
      el.scrollLeft = scrollLeft - walk;
    }

    el.addEventListener('mousedown', onMouseDown);
    el.addEventListener('mouseleave', onMouseLeave);
    el.addEventListener('mouseup', onMouseUp);
    el.addEventListener('mousemove', onMouseMove);

    return () => {
      el.removeEventListener('mousedown', onMouseDown);
      el.removeEventListener('mouseleave', onMouseLeave);
      el.removeEventListener('mouseup', onMouseUp);
      el.removeEventListener('mousemove', onMouseMove);
    };
  }, []);

  return (
    <nav className="top-nav-bar" ref={scrollRef}>
      <div className="top-nav-container">
        <button
          className={`top-nav-item ${activeCategory === '' ? 'active' : ''}`}
          onClick={() => onCategoryChange('')}
        >
          全部
        </button>
        {categories.map((cat) => (
          <button
            key={cat.id}
            className={`top-nav-item ${activeCategory === String(cat.id) ? 'active' : ''}`}
            onClick={() => onCategoryChange(String(cat.id))}
          >
            {cat.name}
          </button>
        ))}
      </div>
    </nav>
  );
}

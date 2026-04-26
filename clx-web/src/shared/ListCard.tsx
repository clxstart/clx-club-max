interface ListCardProps {
  title: string;
  items: string[];
}

// 通用列表卡片组件：用于展示热门帖子、热词等
export function ListCard({ title, items }: ListCardProps) {
  return (
    <div className="card">
      <h2>{title}</h2>
      {items.length > 0
        ? items.map((item, index) => (
            <p className="muted" key={`${item}-${index}`}>{item}</p>
          ))
        : <p className="muted">暂无数据</p>
      }
    </div>
  );
}
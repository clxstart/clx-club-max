import type { ActiveUserVO } from '../../api/types';

interface ActiveRankProps {
  users: ActiveUserVO[];
}

// 月度活跃排行榜组件
export function ActiveRank({ users }: ActiveRankProps) {
  return (
    <div className="aside-card">
      <h3>月度活跃排行</h3>
      <div className="aside-list">
        {users.length === 0 && <p className="muted">暂无数据</p>}
        {users.map((user) => (
          <div className="aside-list-item" key={user.userId}>
            <span className={`rank ${user.rank <= 3 ? 'top3' : ''}`}>{user.rank}</span>
            <span className="avatar">{user.username.charAt(0).toUpperCase()}</span>
            <span className="name">{user.username}</span>
            <span className="score">{user.score}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

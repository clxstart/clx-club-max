import { useCallback, useEffect, useState } from 'react';
import { request } from '@/api';
import type { ApiResponse } from '@/api/response';

interface SocialBind {
  id: number;
  socialType: string;
  socialName: string;
  socialAvatar: string;
  bindTime: string;
}

/**
 * 社交账号绑定管理页面
 */
export const SocialBindPage: React.FC = () => {
  const [binds, setBinds] = useState<SocialBind[]>([]);
  const [loading, setLoading] = useState(true);
  const [unbindingId, setUnbindingId] = useState<number | null>(null);

  // 获取绑定列表
  const fetchBinds = useCallback(async () => {
    setLoading(true);
    try {
      const response = await request.get<ApiResponse<SocialBind[]>>('/auth/bindings');
      if (response.code === 200 && response.data) {
        setBinds(response.data);
      }
    } catch (error) {
      console.error('获取绑定列表失败:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchBinds();
  }, [fetchBinds]);

  // 解绑
  const handleUnbind = useCallback(async (id: number) => {
    if (!confirm('确定要解绑该账号吗？')) return;

    setUnbindingId(id);
    try {
      const response = await request.delete<ApiResponse<void>>(`/auth/bindings/${id}`);
      if (response.code === 200) {
        setBinds((prev) => prev.filter((b) => b.id !== id));
      } else {
        alert(response.msg || '解绑失败');
      }
    } catch (error) {
      alert('解绑失败');
    } finally {
      setUnbindingId(null);
    }
  }, []);

  // 绑定GitHub
  const handleBindGithub = useCallback(async () => {
    try {
      const response = await request.get<ApiResponse<string>>('/auth/bindings/github/authorize');
      if (response.code === 200 && response.data) {
        // 跳转到GitHub授权
        window.location.href = response.data;
      }
    } catch (error) {
      alert('获取授权链接失败');
    }
  }, []);

  // 获取平台显示名称
  const getPlatformName = (type: string) => {
    const names: Record<string, string> = {
      github: 'GitHub',
      qq: 'QQ',
      wechat: '微信',
    };
    return names[type] || type;
  };

  // 获取平台图标
  const getPlatformIcon = (type: string) => {
    if (type === 'github') {
      return (
        <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12c0 4.42 2.87 8.17 6.84 9.49.5.09.68-.22.68-.48l-.01-1.7c-2.78.6-3.37-1.34-3.37-1.34-.45-1.15-1.11-1.46-1.11-1.46-.91-.62.07-.61.07-.61 1 .07 1.53 1.03 1.53 1.03.89 1.52 2.34 1.08 2.91.83.09-.65.35-1.09.63-1.34-2.22-.25-4.55-1.11-4.55-4.94 0-1.09.39-1.98 1.03-2.68-.1-.25-.45-1.27.1-2.64 0 0 .84-.27 2.75 1.02.8-.22 1.65-.33 2.5-.33.85 0 1.7.11 2.5.33 1.91-1.29 2.75-1.02 2.75-1.02.55 1.37.2 2.39.1 2.64.64.7 1.03 1.59 1.03 2.68 0 3.84-2.34 4.68-4.57 4.93.36.31.68.92.68 1.85l-.01 2.75c0 .26.18.58.69.48C19.14 20.16 22 16.42 22 12c0-5.52-4.48-10-10-10z" />
        </svg>
      );
    }
    return null;
  };

  return (
    <div className="p-6">
      <h1 className="text-xl font-bold text-[#39ff14] mb-6">社交账号绑定</h1>

      {loading ? (
        <div className="text-center text-[#39ff14]/50 py-8">加载中...</div>
      ) : (
        <div className="space-y-4">
          {/* 已绑定账号列表 */}
          {binds.map((bind) => (
            <div
              key={bind.id}
              className="flex items-center justify-between p-4 rounded-lg border border-[#39ff14]/20 bg-[#39ff14]/5"
            >
              <div className="flex items-center gap-4">
                {bind.socialAvatar ? (
                  <img src={bind.socialAvatar} alt="" className="w-12 h-12 rounded-full" />
                ) : (
                  <div className="w-12 h-12 rounded-full bg-[#39ff14]/20 flex items-center justify-center text-[#39ff14]">
                    {getPlatformIcon(bind.socialType)}
                  </div>
                )}
                <div>
                  <div className="text-[#39ff14] font-medium">
                    {getPlatformName(bind.socialType)}
                  </div>
                  <div className="text-[#39ff14]/50 text-sm">{bind.socialName}</div>
                  {bind.bindTime && (
                    <div className="text-[#39ff14]/30 text-xs">绑定于 {bind.bindTime}</div>
                  )}
                </div>
              </div>
              <button
                onClick={() => void handleUnbind(bind.id)}
                disabled={unbindingId === bind.id}
                className="px-4 py-2 rounded-lg border border-red-500/30 text-red-400 hover:bg-red-500/10 transition disabled:opacity-50"
              >
                {unbindingId === bind.id ? '解绑中...' : '解绑'}
              </button>
            </div>
          ))}

          {binds.length === 0 && (
            <div className="text-center text-[#39ff14]/50 py-8">暂无绑定的社交账号</div>
          )}

          {/* 绑定新账号 */}
          <div className="pt-6 border-t border-[#39ff14]/20 mt-6">
            <h2 className="text-lg text-[#39ff14] mb-4">绑定新账号</h2>
            <div className="flex gap-4">
              <button
                onClick={() => void handleBindGithub()}
                className="flex items-center gap-2 px-4 py-2 rounded-lg border border-[#39ff14]/30 text-[#39ff14] hover:bg-[#39ff14]/10 transition"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2C6.48 2 2 6.48 2 12c0 4.42 2.87 8.17 6.84 9.49.5.09.68-.22.68-.48l-.01-1.7c-2.78.6-3.37-1.34-3.37-1.34-.45-1.15-1.11-1.46-1.11-1.46-.91-.62.07-.61.07-.61 1 .07 1.53 1.03 1.53 1.03.89 1.52 2.34 1.08 2.91.83.09-.65.35-1.09.63-1.34-2.22-.25-4.55-1.11-4.55-4.94 0-1.09.39-1.98 1.03-2.68-.1-.25-.45-1.27.1-2.64 0 0 .84-.27 2.75 1.02.8-.22 1.65-.33 2.5-.33.85 0 1.7.11 2.5.33 1.91-1.29 2.75-1.02 2.75-1.02.55 1.37.2 2.39.1 2.64.64.7 1.03 1.59 1.03 2.68 0 3.84-2.34 4.68-4.57 4.93.36.31.68.92.68 1.85l-.01 2.75c0 .26.18.58.69.48C19.14 20.16 22 16.42 22 12c0-5.52-4.48-10-10-10z" />
                </svg>
                绑定 GitHub
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SocialBindPage;

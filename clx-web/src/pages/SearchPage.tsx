import { useState } from 'react';
import { Search } from 'lucide-react';
import { searchApi } from '../api';
import type { SearchVO } from '../api/types';

interface SearchPageProps {
  hotKeywords: unknown[];
  setHotKeywords: (items: unknown[]) => void;
  setMessage: (msg: { text: string; error?: boolean }) => void;
  run: <T>(action: () => Promise<T>, ok: string) => Promise<T | undefined>;
}

// 搜索页
export function SearchPage({ hotKeywords, setHotKeywords, setMessage, run }: SearchPageProps) {
  const [keyword, setKeyword] = useState('');
  const [searchResult, setSearchResult] = useState<SearchVO>();

  function itemTitle(item: unknown) {
    if (item && typeof item === 'object') {
      const data = item as Record<string, unknown>;
      return String(data.title || data.name || data.username || data.content || JSON.stringify(data));
    }
    return String(item ?? '暂无标题');
  }

  async function handleSearch(event: React.FormEvent) {
    event.preventDefault();
    const data = await run(() => searchApi.aggregate({ keyword, page: 1, size: 10, enableHighlight: true, enableSuggest: true }), '聚合搜索完成。');
    if (data) setSearchResult(data);
  }

  return (
    <div className="search-page layout">
      <div className="card">
        <h2>聚合搜索</h2>
        <form className="toolbar" onSubmit={handleSearch}>
          <input className="field" placeholder="输入关键词" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
          <button className="soft-btn primary"><Search size={16} /> 搜索</button>
        </form>
        <div className="search-results">
          {Object.entries(searchResult?.results || {}).map(([type, result]) => (
            <div className="post-item" key={type}>
              <h3>{type} · {result.total} 条</h3>
              {result.error && <p className="error">{result.error}</p>}
              {result.items?.slice(0, 5).map((item, index) => <p key={index}>{itemTitle(item)}</p>)}
            </div>
          ))}
        </div>
      </div>
      <div className="card">
        <h2>单类搜索 / 建议</h2>
        <div className="form-grid">
          <button className="soft-btn" onClick={() => keyword && run(() => searchApi.single('post', keyword), '单类搜索完成。').then((data) => data && setSearchResult({ keyword, results: { post: data } }))}>搜索帖子</button>
          <button className="soft-btn" onClick={() => keyword && run(() => searchApi.suggest(keyword), '搜索建议已获取。').then((data) => data && setMessage({ text: `建议：${data.join('、') || '暂无'}` }))}>获取建议</button>
          <button className="soft-btn" onClick={() => run(() => searchApi.hot('day', 10), '热词已刷新。').then((data) => data && setHotKeywords(data))}>刷新热词</button>
        </div>
        <div className="card">
          <h2>热词</h2>
          {hotKeywords.map((item, i) => <p className="muted" key={i}>{itemTitle(item)}</p>)}
        </div>
      </div>
    </div>
  );
}
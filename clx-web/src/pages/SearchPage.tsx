import { useState } from 'react';
import { Search, ChevronDown, ChevronRight } from 'lucide-react';
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
  // 整个搜索结果面板的展开状态
  const [panelOpen, setPanelOpen] = useState(true);
  // 每个分组的展开状态
  const [expanded, setExpanded] = useState<Record<string, boolean>>({});

  function itemTitle(item: unknown) {
    if (item && typeof item === 'object') {
      const data = item as Record<string, unknown>;
      return String(data.title || data.name || data.username || data.content || JSON.stringify(data));
    }
    return String(item ?? '暂无标题');
  }

  function toggleExpand(type: string) {
    setExpanded(prev => ({ ...prev, [type]: !prev[type] }));
  }

  async function handleSearch(event: React.FormEvent) {
    event.preventDefault();
    const data = await run(() => searchApi.aggregate({ keyword, page: 1, size: 10, enableHighlight: true, enableSuggest: true }), '聚合搜索完成。');
    if (data) {
      setSearchResult(data);
      setPanelOpen(true);
      const types = Object.keys(data.results || {});
      const initExpanded: Record<string, boolean> = {};
      types.forEach(t => initExpanded[t] = true);
      setExpanded(initExpanded);
    }
  }

  // 有搜索结果时显示折叠图标
  const hasResult = searchResult && Object.keys(searchResult.results || {}).length > 0;

  return (
    <div className="search-page layout">
      <div className="card">
        <h2>聚合搜索</h2>
        <form className="toolbar" onSubmit={handleSearch}>
          <input className="field" placeholder="输入关键词" value={keyword} onChange={(e) => setKeyword(e.target.value)} />
          <button className="soft-btn primary"><Search size={16} /> 搜索</button>
        </form>

        {/* 搜索结果区域：收起时显示小图标，展开时显示完整面板 */}
        {hasResult && (
          <div className={`search-result-panel ${panelOpen ? 'open' : 'collapsed'}`}>
            {/* 收起状态：只显示小图标 */}
            {!panelOpen && (
              <button className="search-panel-toggle-mini" onClick={() => setPanelOpen(true)}>
                <Search size={16} />
                <span className="toggle-count">
                  {Object.values(searchResult!.results || {}).reduce((sum, r) => sum + (r.total || 0), 0)}
                </span>
              </button>
            )}

            {/* 展开状态：完整面板 */}
            {panelOpen && (
              <>
                <div className="search-panel-header">
                  <span className="panel-title">搜索结果</span>
                  <button className="panel-collapse-btn" onClick={() => setPanelOpen(false)}>
                    <ChevronRight size={14} />
                  </button>
                </div>
                <div className="search-results">
                  {Object.entries(searchResult?.results || {}).map(([type, result]) => (
                    <div className="search-result-group" key={type}>
                      <div className="search-result-header" onClick={() => toggleExpand(type)}>
                        <span className="search-result-icon">
                          {expanded[type] ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                        </span>
                        <h3>{type} · {result.total} 条</h3>
                      </div>
                      {result.error && <p className="error">{result.error}</p>}
                      {expanded[type] && (
                        <div className="search-result-list">
                          {result.items?.slice(0, 5).map((item, index) => (
                            <p className="search-result-item" key={index}>{itemTitle(item)}</p>
                          ))}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </>
            )}
          </div>
        )}
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
import { FormEvent, useState } from 'react';
import { Send, Bold, Italic, Code, List, Link2, Image, Heading1, Heading2 } from 'lucide-react';
import { postApi } from '../api';
import type { CategoryVO, TagVO } from '../api/types';

interface ComposePageProps {
  categories: CategoryVO[];
  tags: TagVO[];
  run: <T>(action: () => Promise<T>, ok: string) => Promise<T | undefined>;
  onCreated: (id: number) => void;
}

// 发帖页（Markdown 编辑器）
export function ComposePage({ categories, tags, run, onCreated }: ComposePageProps) {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);
  const [preview, setPreview] = useState(false);

  // Markdown 工具栏操作
  function insertMarkdown(prefix: string, suffix: string = '') {
    const textarea = document.querySelector('textarea.compose-editor') as HTMLTextAreaElement;
    if (!textarea) return;

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const selected = content.substring(start, end);
    const before = content.substring(0, start);
    const after = content.substring(end);

    const newContent = before + prefix + selected + suffix + after;
    setContent(newContent);

    // 恢复光标位置
    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(start + prefix.length, start + prefix.length + selected.length);
    }, 0);
  }

  function handleBold() { insertMarkdown('**', '**'); }
  function handleItalic() { insertMarkdown('*', '*'); }
  function handleCode() { insertMarkdown('\n```\n', '\n```\n'); }
  function handleHeading1() { insertMarkdown('\n# '); }
  function handleHeading2() { insertMarkdown('\n## '); }
  function handleList() { insertMarkdown('\n- '); }
  function handleLink() { insertMarkdown('[', '](url)'); }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const id = await run(() => postApi.create({
      title,
      content,
      categoryId: categoryId || undefined,
      tagIds: selectedTagIds
    }), '帖子发布成功。');
    if (id) {
      setTitle('');
      setContent('');
      setCategoryId('');
      setSelectedTagIds([]);
      onCreated(id as number);
    }
  }

  // 简易 Markdown 渲染
  function renderMarkdown(text: string): string {
    return text
      .replace(/^# (.+)$/gm, '<h1>$1</h1>')
      .replace(/^## (.+)$/gm, '<h2>$1</h2>')
      .replace(/\*\*(.+)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.+)\*/g, '<em>$1</em>')
      .replace(/`(.+)`/g, '<code>$1</code>')
      .replace(/^```(.*)```$/gm, '<pre>$1</pre>')
      .replace(/^- (.+)$/gm, '<li>$1</li>')
      .replace(/\n/g, '<br/>');
  }

  function toggleTag(tagId: number) {
    if (selectedTagIds.includes(tagId)) {
      setSelectedTagIds(selectedTagIds.filter(id => id !== tagId));
    } else {
      setSelectedTagIds([...selectedTagIds, tagId]);
    }
  }

  return (
    <div className="compose-page">
      <div className="compose-main">
        <div className="card compose-card">
          <h2>发布帖子</h2>

          <form className="compose-form" onSubmit={handleSubmit}>
            {/* 标题 */}
            <div className="form-field">
              <label>标题</label>
              <input
                className="field"
                placeholder="给你的帖子起个标题..."
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                maxLength={100}
              />
            </div>

            {/* 分类 */}
            <div className="form-field">
              <label>分类</label>
              <select
                className="field"
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
              >
                <option value="">选择分类</option>
                {categories.map((item) => (
                  <option key={item.id} value={item.id}>{item.name}</option>
                ))}
              </select>
            </div>

            {/* 标签 */}
            <div className="form-field">
              <label>标签</label>
              <div className="tag-selector">
                {tags.map((tag) => (
                  <button
                    key={tag.id}
                    className={`tag-btn ${selectedTagIds.includes(tag.id) ? 'selected' : ''}`}
                    onClick={() => toggleTag(tag.id)}
                    type="button"
                  >
                    {tag.name}
                  </button>
                ))}
              </div>
            </div>

            {/* Markdown 工具栏 */}
            <div className="form-field">
              <label>内容（支持 Markdown）</label>
              <div className="md-toolbar">
                <button type="button" onClick={handleBold} title="粗体"><Bold size={16} /></button>
                <button type="button" onClick={handleItalic} title="斜体"><Italic size={16} /></button>
                <button type="button" onClick={handleHeading1} title="标题1"><Heading1 size={16} /></button>
                <button type="button" onClick={handleHeading2} title="标题2"><Heading2 size={16} /></button>
                <button type="button" onClick={handleCode} title="代码块"><Code size={16} /></button>
                <button type="button" onClick={handleList} title="列表"><List size={16} /></button>
                <button type="button" onClick={handleLink} title="链接"><Link2 size={16} /></button>
                <div className="toolbar-spacer" />
                <button
                  type="button"
                  className={`preview-btn ${preview ? 'active' : ''}`}
                  onClick={() => setPreview(!preview)}
                >
                  {preview ? '编辑' : '预览'}
                </button>
              </div>

              {/* 编辑/预览切换 */}
              {!preview ? (
                <textarea
                  className="field compose-editor"
                  placeholder="使用 Markdown 编写内容..."
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  rows={15}
                />
              ) : (
                <div
                  className="md-preview"
                  dangerouslySetInnerHTML={{ __html: renderMarkdown(content) || '<p class="empty-tip">暂无内容</p>' }}
                />
              )}
            </div>

            {/* 发布按钮 */}
            <div className="form-actions">
              <button
                className="soft-btn primary"
                disabled={!title.trim() || !content.trim()}
              >
                <Send size={16} />
                发布帖子
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* 侧栏提示 */}
      <div className="compose-aside">
        <div className="card">
          <h3>Markdown 语法提示</h3>
          <div className="md-tips">
            <p><code># 标题</code> 一级标题</p>
            <p><code>## 标题</code> 二级标题</p>
            <p><code>**粗体**</code> 粗体文本</p>
            <p><code>*斜体*</code> 斜体文本</p>
            <p><code>`代码`</code> 行内代码</p>
            <p><code>```代码块```</code> 代码块</p>
            <p><code>- 列表项</code> 无序列表</p>
            <p><code>[链接](url)</code> 超链接</p>
          </div>
        </div>
      </div>
    </div>
  );
}
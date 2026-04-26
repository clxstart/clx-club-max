import { FormEvent, useState } from 'react';
import { Send } from 'lucide-react';
import { postApi } from '../api';
import type { CategoryVO, TagVO } from '../api/types';

interface ComposePageProps {
  categories: CategoryVO[];
  tags: TagVO[];
  run: <T>(action: () => Promise<T>, ok: string) => Promise<T | undefined>;
  onCreated: (id: number) => void;
}

// 发帖页
export function ComposePage({ categories, tags, run, onCreated }: ComposePageProps) {
  const [compose, setCompose] = useState({ title: '', content: '', categoryId: '', tagIds: '' });
  const selectedTags = compose.tagIds.split(',').map((id) => id.trim()).filter(Boolean);

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    const id = await run(() => postApi.create({
      title: compose.title,
      content: compose.content,
      categoryId: compose.categoryId || undefined,
      tagIds: selectedTags
    }), '帖子发布成功。');
    if (id) {
      setCompose({ title: '', content: '', categoryId: '', tagIds: '' });
      onCreated(id as number);
    }
  }

  return (
    <div className="compose-page">
      <div className="card">
        <h2>发布帖子</h2>
        <form className="form-grid" onSubmit={handleCreate}>
          <input
            className="field"
            placeholder="标题"
            value={compose.title}
            onChange={(e) => setCompose({ ...compose, title: e.target.value })}
          />
          <textarea
            className="field"
            placeholder="内容"
            value={compose.content}
            onChange={(e) => setCompose({ ...compose, content: e.target.value })}
          />
          <select
            className="field"
            value={compose.categoryId}
            onChange={(e) => setCompose({ ...compose, categoryId: e.target.value })}
          >
            <option value="">选择分类</option>
            {categories.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}
          </select>
          <input
            className="field"
            placeholder="标签 ID，用英文逗号分隔"
            value={compose.tagIds}
            onChange={(e) => setCompose({ ...compose, tagIds: e.target.value })}
          />
          <button className="soft-btn primary"><Send size={16} /> 发布</button>
        </form>
      </div>
      <div className="side-stack">
        <div className="card">
          <h2>可用分类</h2>
          {categories.map((item) => <p className="muted" key={item.id}>{item.id} · {item.name}</p>)}
        </div>
        <div className="card">
          <h2>可用标签</h2>
          {tags.map((item) => <p className="muted" key={item.id}>{item.id} · {item.name}</p>)}
        </div>
      </div>
    </div>
  );
}
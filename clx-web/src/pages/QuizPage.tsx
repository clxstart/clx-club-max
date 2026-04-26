import { useState, useEffect } from 'react';
import { BookOpen, CheckCircle, XCircle } from 'lucide-react';
import { quizApi } from '../api';
import type { SubjectCategoryVO, SubjectLabelVO, SubjectVO, PracticeSubjectVO, SubmitResultVO, PracticeResultVO, WrongBookVO } from '../api/types';

interface QuizPageProps {
  run: <T>(action: () => Promise<T>, ok: string) => Promise<T | undefined>;
  setMessage: (msg: { text: string; error?: boolean }) => void;
}

// 刷题页
export function QuizPage({ run, setMessage }: QuizPageProps) {
  const [categories, setCategories] = useState<SubjectCategoryVO[]>([]);
  const [labels, setLabels] = useState<SubjectLabelVO[]>([]);
  const [subjects, setSubjects] = useState<SubjectVO[]>([]);
  const [total, setTotal] = useState(0);
  const [filters, setFilters] = useState({ categoryId: '', labelId: '', keyword: '', pageNo: 1, pageSize: 10 });
  const [practiceId, setPracticeId] = useState<number>();
  const [practiceSubjectIds, setPracticeSubjectIds] = useState<number[]>([]);
  const [currentSubjectIndex, setCurrentSubjectIndex] = useState(0);
  const [currentSubject, setCurrentSubject] = useState<PracticeSubjectVO>();
  const [userAnswer, setUserAnswer] = useState('');
  const [submitResult, setSubmitResult] = useState<SubmitResultVO>();
  const [practiceResult, setPracticeResult] = useState<PracticeResultVO>();
  const [wrongBooks, setWrongBooks] = useState<WrongBookVO[]>([]);
  const [wrongBookTotal, setWrongBookTotal] = useState(0);

  useEffect(() => {
    quizApi.categories().then(setCategories).catch(() => undefined);
    quizApi.labels().then(setLabels).catch(() => undefined);
  }, []);

  function loadPracticeSubject(pid: number, sid: number) {
    const subject = subjects.find(s => s.id === sid);
    run(() => quizApi.practiceSubject(pid, sid, subject?.subjectType || 1), '题目已加载。').then((data) => {
      if (data) {
        setCurrentSubject(data);
        setUserAnswer('');
        setSubmitResult(undefined);
      }
    });
  }

  function nextSubject() {
    if (!practiceId) return;
    const nextIndex = currentSubjectIndex + 1;
    if (nextIndex >= practiceSubjectIds.length) {
      run(() => quizApi.practiceFinish(practiceId), '练习已结束。').then((data) => data && setPracticeResult(data));
    } else {
      setCurrentSubjectIndex(nextIndex);
      loadPracticeSubject(practiceId, practiceSubjectIds[nextIndex]);
    }
  }

  return (
    <div className="quiz-page layout">
      {/* 题库 */}
      <div className="card">
        <h2><BookOpen size={20} /> 题库</h2>
        <div className="toolbar">
          <select className="field" value={filters.categoryId} onChange={(e) => setFilters({ ...filters, categoryId: e.target.value })}>
            <option value="">全部分类</option>
            {categories.map((item) => <option key={item.id} value={item.id}>{item.categoryName}</option>)}
          </select>
          <select className="field" value={filters.labelId} onChange={(e) => setFilters({ ...filters, labelId: e.target.value })}>
            <option value="">全部标签</option>
            {labels.map((item) => <option key={item.id} value={item.id}>{item.labelName}</option>)}
          </select>
          <input className="field" placeholder="关键词" value={filters.keyword} onChange={(e) => setFilters({ ...filters, keyword: e.target.value })} />
          <button className="soft-btn primary" onClick={() => run(() => quizApi.subjectPage(filters), '题目列表已刷新。').then((data) => { if (data) { setSubjects(data.list); setTotal(data.total); } })}>搜索</button>
        </div>
        <div className="post-list">
          {subjects.map((item) => (
            <div className="post-item" key={item.id}>
              <h3>{item.subjectName}</h3>
              <p className="muted">类型：{['', '单选', '多选', '判断', '简答'][item.subjectType]} | 难度：{item.subjectDifficult}</p>
              <div className="post-meta">
                <span>{item.categoryName || '未分类'}</span>
                <span>{item.labelNames?.join('、') || '无标签'}</span>
              </div>
            </div>
          ))}
        </div>
        <p className="muted">共 {total} 道题目</p>
      </div>

      {/* 开始练习 */}
      <div className="card">
        <h2>开始练习</h2>
        <div className="form-grid">
          <input className="field" placeholder="标签ID（逗号分隔）" value={filters.labelId} readOnly />
          <input className="field" placeholder="题目数量" type="number" defaultValue={10} id="quizCount" />
          <button className="soft-btn primary" onClick={() => {
            const count = Number((document.getElementById('quizCount') as HTMLInputElement)?.value) || 10;
            const labelIds = filters.labelId ? [Number(filters.labelId)] : undefined;
            run(() => quizApi.practiceStart({ labelIds, count }), '练习已开始。').then((data) => {
              if (data) {
                setPracticeId(data.practiceId);
                setPracticeSubjectIds(data.subjectIds);
                setCurrentSubjectIndex(0);
                setCurrentSubject(undefined);
                setSubmitResult(undefined);
                setPracticeResult(undefined);
                if (data.subjectIds.length > 0) {
                  loadPracticeSubject(data.practiceId, data.subjectIds[0]);
                }
              }
            });
          }}>开始练习</button>
        </div>
      </div>

      {/* 答题区 */}
      <div className="side-stack">
        <div className="card">
          <h2>答题中</h2>
          {!practiceId ? (
            <p className="muted">点击"开始练习"开始答题。</p>
          ) : practiceResult ? (
            <>
              <h3>练习结果</h3>
              <p>正确率：{(practiceResult.correctRate * 100).toFixed(1)}%</p>
              <p>用时：{practiceResult.timeUsed}</p>
              <p>错题：{practiceResult.wrongSubjectIds?.length || 0} 道</p>
              <button className="soft-btn" onClick={() => { setPracticeId(undefined); setPracticeResult(undefined); }}>重新开始</button>
            </>
          ) : (
            <>
              <p className="muted">第 {currentSubjectIndex + 1} / {practiceSubjectIds.length} 题</p>
              {currentSubject && (
                <>
                  <h3>{currentSubject.subjectName}</h3>
                  <p className="muted">类型：{['', '单选', '多选', '判断', '简答'][currentSubject.subjectType]}</p>
                  {currentSubject.optionList?.map((opt, i) => (
                    <div key={i}>
                      <label>
                        <input
                          type={currentSubject.subjectType === 2 ? 'checkbox' : 'radio'}
                          name="answer"
                          value={opt.optionType}
                          onChange={() => setUserAnswer(String(opt.optionType))}
                        />
                        {['A', 'B', 'C', 'D', 'E', 'F'][opt.optionType - 1]}. {opt.optionContent}
                      </label>
                    </div>
                  ))}
                  {currentSubject.subjectType === 4 && (
                    <textarea className="field" placeholder="输入你的答案" value={userAnswer} onChange={(e) => setUserAnswer(e.target.value)} />
                  )}
                  {submitResult && (
                    <>
                      <p className={submitResult.isCorrect === 1 ? '' : 'error'}>
                        {submitResult.isCorrect === 1 ? <><CheckCircle size={16} /> 正确</> : submitResult.isCorrect === 0 ? <><XCircle size={16} /> 错误</> : '需自评'}
                      </p>
                      <p><strong>正确答案：</strong>{submitResult.correctAnswer}</p>
                      <p><strong>解析：</strong>{submitResult.subjectParse}</p>
                      {submitResult.needSelfJudge && (
                        <div className="toolbar">
                          <button className="soft-btn primary" onClick={() => run(() => quizApi.practiceSelfJudge(practiceId!, currentSubject.subjectId, 1), '已标记为正确。').then(() => nextSubject())}>我答对了</button>
                          <button className="soft-btn danger" onClick={() => run(() => quizApi.practiceSelfJudge(practiceId!, currentSubject.subjectId, 0), '已标记为错误。').then(() => nextSubject())}>我答错了</button>
                        </div>
                      )}
                      {!submitResult.needSelfJudge && <button className="soft-btn primary" onClick={() => nextSubject()}>下一题</button>}
                    </>
                  )}
                  {!submitResult && (
                    <button className="soft-btn primary" onClick={() => {
                      if (!userAnswer) { setMessage({ text: '请先选择答案', error: true }); return; }
                      run(() => quizApi.practiceSubmit({ practiceId: practiceId!, subjectId: currentSubject.subjectId, subjectType: currentSubject.subjectType, answerContent: userAnswer }), '答案已提交。').then((data) => data && setSubmitResult(data));
                    }}>提交答案</button>
                  )}
                </>
              )}
            </>
          )}
        </div>

        {/* 错题本 */}
        <div className="card">
          <h2>错题本</h2>
          <button className="soft-btn" onClick={() => run(() => quizApi.wrongBookList(1, 10), '错题本已加载。').then((data) => { if (data) { setWrongBooks(data.list); setWrongBookTotal(data.total); } })}>加载错题本</button>
          {wrongBooks.map((item, i) => <p className="muted" key={i}>{item.subjectName} (错{item.wrongCount}次)</p>)}
          <p className="muted">共 {wrongBookTotal} 道错题</p>
        </div>
      </div>
    </div>
  );
}
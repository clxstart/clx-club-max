import type {
  ApiResponse,
  CaptchaResult,
  CategoryVO,
  CommentCreateRequest,
  CommentVO,
  LoginRequest,
  LoginVO,
  PasswordResetConfirmRequest,
  PasswordResetRequest,
  PhoneLoginRequest,
  PostCreateRequest,
  PostDetailVO,
  PostListRequest,
  PostListVO,
  PostUpdateRequest,
  RegisterRequest,
  RegisterVO,
  SearchRequest,
  SearchResult,
  SearchVO,
  SmsCodeRequest,
  SocialBindVO,
  TagVO,
  UserInfoVO,
  HotKeywordVO,
  SubjectCategoryVO,
  SubjectLabelVO,
  SubjectVO,
  SubjectDetailVO,
  SubjectQueryRequest,
  SubjectCreateRequest,
  PracticeStartRequest,
  PracticeSubjectVO,
  PracticeSubmitRequest,
  SubmitResultVO,
  PracticeResultVO,
  WrongBookVO
} from './types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';
const TOKEN_KEY = 'clx_token';
const TOKEN_NAME_KEY = 'clx_token_name';

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export function saveToken(payload: LoginVO | RegisterVO) {
  const token = payload.tokenValue || payload.token;
  if (!token) return;
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(TOKEN_NAME_KEY, payload.tokenName || 'satoken');
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(TOKEN_NAME_KEY);
}

function query(params?: Record<string, unknown>) {
  const search = new URLSearchParams();
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, String(value));
  });
  const value = search.toString();
  return value ? `?${value}` : '';
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getStoredToken();
  const tokenName = localStorage.getItem(TOKEN_NAME_KEY) || 'satoken';
  const headers = new Headers(options.headers);

  if (!(options.body instanceof FormData) && options.body !== undefined) {
    headers.set('Content-Type', 'application/json');
  }
  if (token) {
    headers.set(tokenName, token);
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    credentials: 'include'
  });

  const text = await response.text();
  const json = text ? JSON.parse(text) as ApiResponse<T> : undefined;

  if (!response.ok) {
    throw new Error(json?.msg || `请求失败：${response.status}`);
  }
  if (json && json.code !== 200) {
    throw new Error(json.msg || '接口返回失败');
  }
  return json?.data as T;
}

const get = <T>(path: string, params?: Record<string, unknown>) => request<T>(`${path}${query(params)}`);
const post = <T>(path: string, body?: unknown) => request<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) });
const put = <T>(path: string, body?: unknown) => request<T>(path, { method: 'PUT', body: body === undefined ? undefined : JSON.stringify(body) });
const del = <T>(path: string) => request<T>(path, { method: 'DELETE' });

export const authApi = {
  captcha: () => get<CaptchaResult>('/auth/captcha'),
  login: (body: LoginRequest) => post<LoginVO>('/auth/login', body),
  register: (body: RegisterRequest) => post<RegisterVO>('/auth/register', body),
  logout: () => post<void>('/auth/logout'),
  me: () => get<UserInfoVO>('/auth/me'),
  refresh: () => post<LoginVO>('/auth/refresh'),
  sendEmailCode: (email: string) => post<void>('/auth/email-code/send', { email }),
  sendSmsCode: (body: SmsCodeRequest) => post<string>('/auth/sms-code/send', body),
  sendPasswordReset: (body: PasswordResetRequest) => post<void>('/auth/password-reset/send', body),
  confirmPasswordReset: (body: PasswordResetConfirmRequest) => post<void>('/auth/password-reset/confirm', body),
  phoneSmsCode: (body: SmsCodeRequest) => post<string>('/auth/phone/sms-code', body),
  phoneLogin: (body: PhoneLoginRequest) => post<LoginVO>('/auth/phone/login', body),
  oauthAuthorize: (platform: string, redirect = false) => get<string>(`/auth/oauth/${platform}/authorize`, { redirect }),
  bindings: () => get<SocialBindVO[]>('/auth/bindings'),
  unbind: (id: number) => del<void>(`/auth/bindings/${id}`),
  bindAuthorize: (platform: string) => get<string>(`/auth/bindings/${platform}/authorize`),
  bindCallback: (platform: string, body: { code: string; state: string }) => post<SocialBindVO>(`/auth/bindings/${platform}/callback`, body)
};

export const postApi = {
  create: (body: PostCreateRequest) => post<number>('/post/create', body),
  update: (id: number, body: PostUpdateRequest) => put<void>(`/post/${id}`, body),
  remove: (id: number) => del<void>(`/post/${id}`),
  detail: (id: number) => get<PostDetailVO>(`/post/${id}`),
  list: (params: PostListRequest) => get<PostListVO>('/post/list', params),
  search: (keyword: string, page = 1, size = 20) => get<PostListVO>('/post/search', { keyword, page, size }),
  hot: (limit = 10) => get<PostListItemVO[]>('/post/hot', { limit }),
  comments: (postId: number) => get<CommentVO[]>(`/post/${postId}/comments`),
  addComment: (postId: number, body: CommentCreateRequest) => post<number>(`/post/${postId}/comment`, body),
  deleteComment: (postId: number, commentId: number) => del<void>(`/post/${postId}/comment/${commentId}`),
  likePost: (id: number) => post<{ likeCount: number }>(`/post/${id}/like`),
  unlikePost: (id: number) => del<{ likeCount: number }>(`/post/${id}/like`),
  likeComment: (id: number) => post<{ likeCount: number }>(`/comment/${id}/like`),
  unlikeComment: (id: number) => del<{ likeCount: number }>(`/comment/${id}/like`)
};

export const taxonomyApi = {
  categories: () => get<CategoryVO[]>('/category/list'),
  tags: () => get<TagVO[]>('/tag/list')
};

export const searchApi = {
  aggregate: (body: SearchRequest) => post<SearchVO>('/search/aggregate', body),
  single: (type: string, keyword: string, page = 1, size = 10) => get<SearchResult>('/search/single', { type, keyword, page, size }),
  suggest: (keyword: string, size = 5) => get<string[]>('/search/suggest', { keyword, size }),
  hot: (period = 'day', limit = 10) => get<HotKeywordVO[]>('/search/hot', { period, limit })
};

export const quizApi = {
  // 分类标签
  categories: () => get<SubjectCategoryVO[]>('/quiz/category/list'),
  labels: (categoryId?: number) => get<SubjectLabelVO[]>('/quiz/label/list', categoryId ? { categoryId } : undefined),

  // 题目管理
  subjectAdd: (body: SubjectCreateRequest) => post<boolean>('/quiz/subject/add', body),
  subjectPage: (body: SubjectQueryRequest) => post<{ total: number; list: SubjectVO[] }>('/quiz/subject/page', body),
  subjectDetail: (id: number) => post<SubjectDetailVO>('/quiz/subject/detail', { id }),
  subjectDelete: (id: number) => post<boolean>(`/quiz/subject/delete/${id}`),

  // 练习流程
  practiceStart: (body: PracticeStartRequest) => post<{ practiceId: number; totalCount: number; subjectIds: number[] }>('/quiz/practice/start', body),
  practiceSubject: (practiceId: number, subjectId: number, subjectType: number) => post<PracticeSubjectVO>('/quiz/practice/subject', { practiceId, subjectId, subjectType }),
  practiceSubmit: (body: PracticeSubmitRequest) => post<SubmitResultVO>('/quiz/practice/submit', body),
  practiceSelfJudge: (practiceId: number, subjectId: number, isCorrect: number) => post<boolean>('/quiz/practice/self-judge', { practiceId, subjectId, isCorrect }),
  practiceFinish: (practiceId: number) => post<PracticeResultVO>('/quiz/practice/finish', { practiceId }),

  // 错题本
  wrongBookList: (pageNo = 1, pageSize = 10) => post<{ total: number; list: WrongBookVO[] }>('/quiz/wrong-book/list', { pageNo, pageSize }),
  wrongBookRemove: (subjectId: number) => post<boolean>('/quiz/wrong-book/remove', { subjectId })
};

export type ApiResponse<T> = {
  code: number;
  msg: string;
  data: T;
  timestamp?: number;
};

export type CaptchaResult = {
  id: string;
  image: string;
};

export type LoginRequest = {
  username: string;
  password: string;
  captchaId: string;
  captchaCode: string;
  rememberMe?: boolean;
};

export type RegisterRequest = {
  username: string;
  password: string;
  confirmPassword: string;
  nickname?: string;
  email: string;
  emailCode: string;
};

export type SmsCodeRequest = {
  phone: string;
  captchaId: string;
  captchaCode: string;
};

export type PhoneLoginRequest = {
  phone: string;
  smsCode: string;
};

export type PasswordResetRequest = {
  email: string;
  captchaId: string;
  captchaCode: string;
};

export type PasswordResetConfirmRequest = {
  email: string;
  resetCode: string;
  newPassword: string;
  confirmPassword: string;
};

export type LoginVO = {
  tokenName?: string;
  tokenValue?: string;
  token?: string;
  userId?: number;
  username?: string;
  nickname?: string;
  expiresIn?: number;
  [key: string]: unknown;
};

export type RegisterVO = LoginVO;

export type UserInfoVO = {
  userId: number;
  username: string;
  tokenInfo?: unknown;
};

export type SocialBindVO = {
  id: number;
  socialType: string;
  socialName: string;
  socialAvatar?: string;
  bindTime?: string;
};

export type AuthorVO = {
  id: number;
  name: string;
  avatar?: string;
};

export type CategoryVO = {
  id: number;
  name: string;
  code?: string;
  description?: string;
  icon?: string;
  postCount?: number;
};

export type TagVO = {
  id: number;
  name: string;
  description?: string;
  color?: string;
  postCount?: number;
};

export type PostListItemVO = {
  id: number;
  title: string;
  summary?: string;
  author?: AuthorVO;
  category?: CategoryVO;
  tags?: TagVO[];
  likeCount?: number;
  commentCount?: number;
  createdAt?: string;
};

export type PostDetailVO = PostListItemVO & {
  content: string;
  viewCount?: number;
  isLiked?: boolean;
};

export type PostListVO = {
  posts: PostListItemVO[];
  total: number;
  page: number;
  size: number;
};

export type PostListRequest = {
  page?: number;
  size?: number;
  sort?: 'latest' | 'hot' | 'recommend' | string;
  categoryId?: number | string;
  tagId?: number | string;
};

export type PostCreateRequest = {
  title: string;
  content: string;
  categoryId?: number | string;
  tagIds?: Array<number | string>;
};

export type PostUpdateRequest = PostCreateRequest;

export type CommentCreateRequest = {
  content: string;
  parentId?: number;
  replyToId?: number;
};

export type CommentVO = {
  id: number;
  content: string;
  author?: AuthorVO;
  likeCount?: number;
  isLiked?: boolean;
  createdAt?: string;
  children?: CommentVO[];
};

export type SearchRequest = {
  keyword: string;
  types?: string[];
  page?: number;
  size?: number;
  enableHighlight?: boolean;
  enableSuggest?: boolean;
};

export type SearchResult = {
  total: number;
  items: unknown[];
  error?: string;
};

export type SearchVO = {
  keyword: string;
  totalTime?: number;
  results?: Record<string, SearchResult>;
  suggest?: string[];
  partialSuccess?: boolean;
};

export type HotKeywordVO = {
  keyword?: string;
  count?: number;
  score?: number;
  [key: string]: unknown;
};

// ========== Quiz Types ==========

export type SubjectCategoryVO = {
  id: number;
  categoryName: string;
  parentId: number;
  sortNum: number;
};

export type SubjectLabelVO = {
  id: number;
  labelName: string;
  categoryId?: number;
  sortNum: number;
};

export type SubjectOptionDTO = {
  optionType: number;
  optionContent: string;
  isCorrect?: number;
};

export type SubjectVO = {
  id: number;
  subjectName: string;
  subjectType: number;
  subjectDifficult: number;
  categoryName?: string;
  labelNames?: string[];
};

export type SubjectDetailVO = {
  id: number;
  subjectName: string;
  subjectType: number;
  subjectDifficult: number;
  subjectScore: number;
  subjectParse: string;
  categoryIds?: number[];
  labelIds?: number[];
  optionList?: SubjectOptionDTO[];
};

export type SubjectQueryRequest = {
  categoryId?: number;
  labelId?: number;
  keyword?: string;
  pageNo?: number;
  pageSize?: number;
};

export type SubjectCreateRequest = {
  subjectName: string;
  subjectType: number;
  subjectDifficult: number;
  subjectScore?: number;
  subjectParse?: string;
  categoryIds?: number[];
  labelIds?: number[];
  optionList?: SubjectOptionDTO[];
};

export type PracticeStartRequest = {
  labelIds?: number[];
  count?: number;
};

export type PracticeSubjectVO = {
  subjectId: number;
  subjectName: string;
  subjectType: number;
  subjectDifficult: number;
  optionList?: SubjectOptionDTO[];
};

export type PracticeSubmitRequest = {
  practiceId: number;
  subjectId: number;
  subjectType: number;
  answerContent: string;
};

export type SubmitResultVO = {
  isCorrect: number;
  correctAnswer: string;
  subjectParse: string;
  needSelfJudge?: boolean;
};

export type PracticeResultVO = {
  totalCount: number;
  correctCount: number;
  correctRate: number;
  timeUsed: string;
  wrongSubjectIds?: number[];
};

export type WrongBookVO = {
  subjectId: number;
  subjectName?: string;
  subjectType?: number;
  wrongCount: number;
  lastWrongTime?: string;
};

// ========== Active User Types ==========

export type ActiveUserVO = {
  rank: number;
  userId: number;
  username: string;
  score: number;
};

// ========== User Profile Types ==========

export type UserProfileVO = {
  userId: number;
  username: string;
  nickname?: string;
  avatar?: string;
  signature?: string;
  gender?: string;
  followCount?: number;
  fansCount?: number;
  likeTotalCount?: number;
  isFollowed?: boolean;
};

export type UserSimpleVO = {
  userId: number;
  nickname?: string;
  avatar?: string;
  signature?: string;
};

export type ProfileUpdateRequest = {
  nickname?: string;
  avatar?: string;
  signature?: string;
  gender?: string;
};

export type FavoriteItemVO = {
  postId: number;
  title: string;
  summary?: string;
  authorName?: string;
  likeCount?: number;
  createdAt?: string;
  favoritedAt?: string;
};

// ========== Message Types ==========

export type ChatSessionVO = {
  sessionId: number;
  targetUserId: number;
  targetNickname: string;
  targetAvatar?: string;
  lastMessage?: string;
  lastTime?: number;  // timestamp in milliseconds
  unreadCount: number;
};

export type ChatMessageVO = {
  messageId: number;
  fromUserId: number;
  content: string;
  timestamp?: number;  // timestamp in milliseconds
  direction: 'sent' | 'received';
  isSelf: boolean;  // computed by frontend
};

export type SendMessageRequest = {
  toUserId: number;
  content: string;
};

export type NotificationVO = {
  id: number;
  type: string;
  title: string;
  content: string;
  isRead: boolean;
  createTime?: number;  // timestamp in milliseconds
  aggregateCount?: number;
};

export type UnreadCountVO = {
  chat?: number;
  comment?: number;
  like?: number;
  follow?: number;
  system?: number;
  total?: number;
};

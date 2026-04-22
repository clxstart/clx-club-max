/**
 * 错误码常量 - 与后端 ResponseCode 保持一致
 *
 * 错误码规则：
 * - 200: 成功
 * - 400-499: 客户端错误（参数错误、验证失败等）
 * - 401: 未登录/Token失效
 * - 403: 权限不足
 * - 500-599: 服务端错误
 * - 1000-1999: 认证模块错误
 * - 2000-2999: 用户模块错误
 * - 3000-3999: 帖子模块错误
 */
export const ErrorCode = {
  // 成功状态
  SUCCESS: 200,

  // 通用客户端错误 400-499
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  METHOD_NOT_ALLOWED: 405,
  CONFLICT: 409,
  TOO_MANY_REQUESTS: 429,

  // 服务端错误 500-599
  INTERNAL_ERROR: 500,
  SERVICE_UNAVAILABLE: 503,

  // 认证模块 1000-1999
  LOGIN_FAILED: 1001,
  ACCOUNT_DISABLED: 1002,
  ACCOUNT_LOCKED: 1003,
  ACCOUNT_NOT_FOUND: 1004,
  TOO_MANY_LOGIN_ATTEMPTS: 1005,
  CAPTCHA_ERROR: 1010,
  CAPTCHA_EXPIRED: 1011,
  EMAIL_CODE_ERROR: 1012,
  EMAIL_CODE_EXPIRED: 1013,
  SMS_CODE_ERROR: 1014,
  SMS_SEND_FAILED: 1015,
  EMAIL_SEND_FAILED: 1016,
  CODE_ALREADY_SENT: 1017,
  PASSWORD_MISMATCH: 1020,
  PASSWORD_TOO_SHORT: 1021,
  PASSWORD_TOO_LONG: 1022,
  USERNAME_EXISTS: 1030,
  EMAIL_EXISTS: 1031,
  PHONE_EXISTS: 1032,
  USERNAME_INVALID: 1033,
  EMAIL_INVALID: 1034,
  PHONE_INVALID: 1035,
  RESET_CODE_ERROR: 1040,
  RESET_CODE_EXPIRED: 1041,

  // 用户模块 2000-2999
  USER_NOT_FOUND: 2001,
  USER_DISABLED: 2002,
  USER_LOCKED: 2003,
  USER_ALREADY_EXISTS: 2004,
  PROFILE_UPDATE_FAILED: 2010,
  AVATAR_UPDATE_FAILED: 2011,
  PASSWORD_UPDATE_FAILED: 2012,

  // 帖子模块 3000-3999
  POST_NOT_FOUND: 3001,
  POST_DELETED: 3002,
  POST_LOCKED: 3003,
  POST_HIDDEN: 3004,
  POST_CREATE_FAILED: 3010,
  POST_UPDATE_FAILED: 3011,
  POST_DELETE_FAILED: 3012,
  COMMENT_NOT_FOUND: 3050,

  // 评论模块 3100-3199
  COMMENT_CREATE_FAILED: 3101,
  COMMENT_DELETE_FAILED: 3102,

  // 文件模块 4000-4099
  FILE_UPLOAD_FAILED: 4001,
  FILE_TOO_LARGE: 4002,
  FILE_TYPE_NOT_ALLOWED: 4003,
  FILE_NOT_FOUND: 4004,
} as const;

export type ErrorCodeType = typeof ErrorCode[keyof typeof ErrorCode];

/**
 * 错误码对应的友好提示信息
 */
export const ErrorMessages: Record<number, string> = {
  [ErrorCode.SUCCESS]: '操作成功',

  // 通用错误
  [ErrorCode.BAD_REQUEST]: '请求参数错误，请检查输入',
  [ErrorCode.UNAUTHORIZED]: '请先登录',
  [ErrorCode.FORBIDDEN]: '没有权限访问此功能',
  [ErrorCode.NOT_FOUND]: '请求的资源不存在',
  [ErrorCode.METHOD_NOT_ALLOWED]: '请求方法不支持',
  [ErrorCode.CONFLICT]: '资源已存在，请勿重复操作',
  [ErrorCode.TOO_MANY_REQUESTS]: '操作过于频繁，请稍后重试',

  // 服务端错误
  [ErrorCode.INTERNAL_ERROR]: '系统繁忙，请稍后重试',
  [ErrorCode.SERVICE_UNAVAILABLE]: '服务暂时不可用，请稍后重试',

  // 认证模块
  [ErrorCode.LOGIN_FAILED]: '用户名或密码错误，请重新输入',
  [ErrorCode.ACCOUNT_DISABLED]: '账号已被禁用，请联系管理员',
  [ErrorCode.ACCOUNT_LOCKED]: '账号已被锁定，请稍后重试或联系管理员',
  [ErrorCode.ACCOUNT_NOT_FOUND]: '账号不存在，请先注册',
  [ErrorCode.TOO_MANY_LOGIN_ATTEMPTS]: '登录失败次数过多，请30分钟后再试',
  [ErrorCode.CAPTCHA_ERROR]: '图形验证码错误，请重新输入',
  [ErrorCode.CAPTCHA_EXPIRED]: '图形验证码已过期，请刷新后重新输入',
  [ErrorCode.EMAIL_CODE_ERROR]: '邮箱验证码错误，请重新输入',
  [ErrorCode.EMAIL_CODE_EXPIRED]: '邮箱验证码已过期，请重新获取',
  [ErrorCode.SMS_CODE_ERROR]: '短信验证码错误，请重新输入',
  [ErrorCode.SMS_SEND_FAILED]: '短信发送失败，请稍后重试',
  [ErrorCode.EMAIL_SEND_FAILED]: '邮件发送失败，请检查邮箱地址',
  [ErrorCode.CODE_ALREADY_SENT]: '验证码已发送，请勿重复请求',
  [ErrorCode.PASSWORD_MISMATCH]: '两次输入的密码不一致',
  [ErrorCode.PASSWORD_TOO_SHORT]: '密码长度不足8位',
  [ErrorCode.PASSWORD_TOO_LONG]: '密码长度超出限制',
  [ErrorCode.USERNAME_EXISTS]: '用户名已存在，请更换',
  [ErrorCode.EMAIL_EXISTS]: '邮箱已存在，请更换或直接登录',
  [ErrorCode.PHONE_EXISTS]: '手机号已存在，请更换',
  [ErrorCode.USERNAME_INVALID]: '用户名格式不正确，仅支持字母数字下划线',
  [ErrorCode.EMAIL_INVALID]: '邮箱格式不正确',
  [ErrorCode.PHONE_INVALID]: '手机号格式不正确',
  [ErrorCode.RESET_CODE_ERROR]: '密码重置码错误，请重新获取',
  [ErrorCode.RESET_CODE_EXPIRED]: '密码重置码已过期，请重新获取',

  // 用户模块
  [ErrorCode.USER_NOT_FOUND]: '用户不存在',
  [ErrorCode.USER_DISABLED]: '用户已被禁用',
  [ErrorCode.USER_LOCKED]: '用户已被锁定',
  [ErrorCode.USER_ALREADY_EXISTS]: '用户已存在',
  [ErrorCode.PROFILE_UPDATE_FAILED]: '个人资料更新失败',
  [ErrorCode.AVATAR_UPDATE_FAILED]: '头像更新失败',
  [ErrorCode.PASSWORD_UPDATE_FAILED]: '密码修改失败',

  // 帖子模块
  [ErrorCode.POST_NOT_FOUND]: '帖子不存在',
  [ErrorCode.POST_DELETED]: '帖子已被删除',
  [ErrorCode.POST_LOCKED]: '帖子已锁定，无法操作',
  [ErrorCode.POST_HIDDEN]: '帖子已隐藏',
  [ErrorCode.POST_CREATE_FAILED]: '帖子发布失败',
  [ErrorCode.POST_UPDATE_FAILED]: '帖子更新失败',
  [ErrorCode.POST_DELETE_FAILED]: '帖子删除失败',
  [ErrorCode.COMMENT_NOT_FOUND]: '评论不存在',

  // 评论模块
  [ErrorCode.COMMENT_CREATE_FAILED]: '评论发布失败',
  [ErrorCode.COMMENT_DELETE_FAILED]: '评论删除失败',

  // 文件模块
  [ErrorCode.FILE_UPLOAD_FAILED]: '文件上传失败',
  [ErrorCode.FILE_TOO_LARGE]: '文件大小超出限制',
  [ErrorCode.FILE_TYPE_NOT_ALLOWED]: '文件类型不允许上传',
  [ErrorCode.FILE_NOT_FOUND]: '文件不存在',
};

/**
 * 根据错误码获取友好提示
 */
export function getErrorMessage(code: number, fallback?: string): string {
  const message = ErrorMessages[code];
  if (message) {
    return message;
  }

  if (fallback) {
    return fallback;
  }

  // 根据 code 范围返回默认提示
  if (code >= 400 && code < 500) {
    return '请求错误，请检查输入或联系客服';
  }
  if (code >= 500 && code < 600) {
    return '系统错误，请稍后重试';
  }
  if (code >= 1000) {
    return '操作失败，请稍后重试';
  }

  return '未知错误，请稍后重试';
}

/**
 * 判断错误码是否为认证相关（需要重新登录）
 */
export function isAuthError(code: number): boolean {
  return code === ErrorCode.UNAUTHORIZED;
}

/**
 * 判断错误码是否为验证码相关
 */
export function isCaptchaError(code: number): boolean {
  return code === ErrorCode.CAPTCHA_ERROR || code === ErrorCode.CAPTCHA_EXPIRED;
}

/**
 * 判断错误码是否为验证码发送相关
 */
export function isCodeSendError(code: number): boolean {
  return (
    code === ErrorCode.EMAIL_CODE_ERROR ||
    code === ErrorCode.EMAIL_CODE_EXPIRED ||
    code === ErrorCode.EMAIL_SEND_FAILED ||
    code === ErrorCode.SMS_CODE_ERROR ||
    code === ErrorCode.SMS_SEND_FAILED ||
    code === ErrorCode.CODE_ALREADY_SENT
  );
}
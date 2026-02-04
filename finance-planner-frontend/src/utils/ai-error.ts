export const RATE_LIMIT_CODE = 4001;

export function mapAiError(code?: number, message?: string): string | undefined {
  if (code === RATE_LIMIT_CODE) {
    return "今日对话次数已用完，请明天再试或升级会员";
  }
  if (!message) return undefined;
  if (message.includes("今日对话次数已用完")) {
    return "今日对话次数已用完，请明天再试或升级会员";
  }
  return message;
}

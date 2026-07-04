import request from '@/utils/request'

/**
 * 生成聊天记录摘要
 */
export function generateSummary(conversationId, messageLimit = 100, summaryType = 'brief') {
  return request({
    url: '/ai/summary',
    method: 'post',
    data: {
      conversationId,
      messageLimit,
      summaryType
    }
  })
}

/**
 * 翻译文本
 */
export function translateText(text, targetLanguage = 'en', sourceLanguage = 'auto') {
  return request({
    url: '/ai/translate',
    method: 'post',
    data: {
      text,
      sourceLanguage,
      targetLanguage
    }
  })
}

/**
 * 翻译消息
 */
export function translateMessage(messageId, targetLanguage = 'en') {
  return request({
    url: '/ai/translate/message',
    method: 'post',
    params: {
      messageId,
      targetLanguage
    }
  })
}

/**
 * 获取智能回复建议
 */
export function suggestReplies(conversationId) {
  return request({
    url: '/ai/suggest-replies',
    method: 'get',
    params: { conversationId }
  })
}

/**
 * 支持的语言列表
 */
export const supportedLanguages = [
  { code: 'zh', name: '中文', nativeName: '中文' },
  { code: 'en', name: '英语', nativeName: 'English' },
  { code: 'ja', name: '日语', nativeName: '日本語' },
  { code: 'ko', name: '韩语', nativeName: '한국어' },
  { code: 'fr', name: '法语', nativeName: 'Français' },
  { code: 'de', name: '德语', nativeName: 'Deutsch' },
  { code: 'es', name: '西班牙语', nativeName: 'Español' },
  { code: 'ru', name: '俄语', nativeName: 'Русский' },
  { code: 'ar', name: '阿拉伯语', nativeName: 'العربية' },
  { code: 'pt', name: '葡萄牙语', nativeName: 'Português' }
]

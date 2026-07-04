import request from '@/utils/request'

/**
 * 发送消息
 */
export function sendMessage(data) {
  // 确保conversationId是字符串，避免BIGINT精度丢失
  const payload = {
    ...data,
    conversationId: String(data.conversationId)
  }
  return request({
    url: '/message/send',
    method: 'post',
    data: payload
  })
}

/**
 * 获取会话消息列表
 */
export function getMessageList(conversationId, page = 1, size = 20) {
  return request({
    url: '/message/list',
    method: 'get',
    params: { conversationId: String(conversationId), page, size }
  })
}

/**
 * 获取消息详情
 */
export function getMessage(messageId) {
  return request({
    url: `/message/${messageId}`,
    method: 'get'
  })
}

/**
 * 撤回消息
 */
export function recallMessage(messageId) {
  return request({
    url: `/message/${messageId}/recall`,
    method: 'post'
  })
}

/**
 * 标记消息已读
 */
export function markMessageAsRead(messageId) {
  return request({
    url: `/message/${String(messageId)}/read`,
    method: 'post'
  })
}

/**
 * 标记会话消息已读
 */
export function markConversationMessagesAsRead(conversationId) {
  return request({
    url: `/message/conversation/${String(conversationId)}/read`,
    method: 'post'
  })
}

/**
 * 获取未读消息数
 */
export function getUnreadMessageCount(conversationId) {
  return request({
    url: `/message/conversation/${String(conversationId)}/unread`,
    method: 'get'
  })
}

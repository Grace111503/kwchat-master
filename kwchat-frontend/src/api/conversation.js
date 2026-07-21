import request from '@/utils/request'

/**
 * 获取会话列表
 */
export function getConversationList() {
  return request({
    url: '/conversation/list',
    method: 'get'
  })
}

/**
 * 获取或创建单聊会话
 */
export function getOrCreatePrivateConversation(targetUserId) {
  return request({
    url: '/conversation/private',
    method: 'post',
    params: { targetUserId: String(targetUserId) }
  })
}

/**
 * 创建群聊会话
 */
export function createGroupConversation(data) {
  return request({
    url: '/conversation/group',
    method: 'post',
    data
  })
}

/**
 * 获取会话详情
 */
export function getConversation(conversationId) {
  return request({
    url: `/conversation/${String(conversationId)}`,
    method: 'get'
  })
}

/**
 * 获取会话成员列表
 */
export function getConversationMembers(conversationId) {
  return request({
    url: `/conversation/${String(conversationId)}/members`,
    method: 'get'
  })
}

/**
 * 添加会话成员
 */
export function addConversationMember(conversationId, userId) {
  return request({
    url: `/conversation/${String(conversationId)}/members`,
    method: 'post',
    params: { userId: String(userId) }
  })
}

/**
 * 移除会话成员
 */
export function removeConversationMember(conversationId, userId) {
  return request({
    url: `/conversation/${String(conversationId)}/members/${String(userId)}`,
    method: 'delete'
  })
}

/**
 * 清除未读消息数
 */
export function clearUnreadCount(conversationId) {
  return request({
    url: `/conversation/${String(conversationId)}/read`,
    method: 'put'
  })
}

/**
 * 设置免打扰
 */
export function setDoNotDisturb(conversationId, doNotDisturb) {
  return request({
    url: `/conversation/${String(conversationId)}/disturb`,
    method: 'put',
    data: { doNotDisturb }
  })
}

/**
 * 设置置顶
 */
export function setTop(conversationId, isTop) {
  return request({
    url: `/conversation/${String(conversationId)}/top`,
    method: 'put',
    data: { isTop }
  })
}

/**
 * 更新群公告
 */
export function updateAnnouncement(conversationId, announcement) {
  return request({
    url: `/conversation/${String(conversationId)}/announcement`,
    method: 'put',
    data: { announcement }
  })
}

/**
 * 更新群名称
 */
export function updateGroupName(conversationId, name) {
  return request({
    url: `/conversation/${String(conversationId)}/name`,
    method: 'put',
    data: { name }
  })
}

/**
 * 更新群头像
 */
export function updateGroupAvatar(conversationId, avatar) {
  return request({
    url: `/conversation/${String(conversationId)}/avatar`,
    method: 'put',
    data: { avatar }
  })
}

/**
 * 解散群聊
 */
export function dissolveGroup(conversationId) {
  return request({
    url: `/conversation/${String(conversationId)}`,
    method: 'delete'
  })
}

/**
 * 更新成员角色
 */
export function updateMemberRole(conversationId, userId, role) {
  return request({
    url: `/conversation/${String(conversationId)}/members/${String(userId)}/role`,
    method: 'put',
    data: { role }
  })
}

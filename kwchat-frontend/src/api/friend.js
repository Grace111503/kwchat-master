import request from '@/utils/request'

/**
 * 发送好友申请
 */
export function sendFriendRequest(receiverId, message) {
  return request({
    url: '/friend/request',
    method: 'post',
    params: { receiverId: String(receiverId), message: message || '' }
  })
}

/**
 * 处理好友申请
 */
export function handleFriendRequest(requestId, status) {
  return request({
    url: `/friend/request/${String(requestId)}`,
    method: 'put',
    data: { status }
  })
}

/**
 * 获取好友列表
 */
export function getFriendList() {
  return request({
    url: '/friend/list',
    method: 'get'
  })
}

/**
 * 获取收到的好友申请
 */
export function getReceivedFriendRequests() {
  return request({
    url: '/friend/requests/received',
    method: 'get'
  })
}

/**
 * 获取发出的好友申请
 */
export function getSentFriendRequests() {
  return request({
    url: '/friend/requests/sent',
    method: 'get'
  })
}

/**
 * 删除好友
 */
export function deleteFriend(friendId) {
  return request({
    url: `/friend/${String(friendId)}`,
    method: 'delete'
  })
}

/**
 * 更新好友备注
 */
export function updateFriendRemark(friendId, remark) {
  return request({
    url: `/friend/${String(friendId)}/remark`,
    method: 'put',
    data: { remark }
  })
}

/**
 * 拉黑好友
 */
export function blackFriend(friendId) {
  return request({
    url: `/friend/${String(friendId)}/black`,
    method: 'put'
  })
}

/**
 * 取消拉黑
 */
export function unblackFriend(friendId) {
  return request({
    url: `/friend/${String(friendId)}/unblack`,
    method: 'put'
  })
}

/**
 * 获取黑名单列表
 */
export function getBlacklist() {
  return request({
    url: '/friend/blacklist',
    method: 'get'
  })
}

/**
 * 更新好友分组
 */
export function updateFriendGroup(friendId, groupName) {
  return request({
    url: `/friend/${String(friendId)}/group`,
    method: 'put',
    data: { groupName }
  })
}

/**
 * 获取好友分组列表
 */
export function getFriendGroups() {
  return request({
    url: '/friend/groups',
    method: 'get'
  })
}

/**
 * 检查是否是好友
 */
export function checkIsFriend(friendId) {
  return request({
    url: `/friend/${String(friendId)}/check`,
    method: 'get'
  })
}

/**
 * 检查黑名单状态
 * @param {number} userId - 要检查的用户ID
 * @returns {Promise<{isBlocked: boolean, isBlockedBy: boolean}>}
 */
export function checkBlacklistStatus(userId) {
  return request({
    url: `/friend/check-blacklist/${String(userId)}`,
    method: 'get'
  })
}

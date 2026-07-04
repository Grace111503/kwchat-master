import request from '@/utils/request'

/**
 * 发送好友申请
 */
export function sendFriendRequest(receiverId, message) {
  return request({
    url: '/friend/request',
    method: 'post',
    params: { receiverId: String(receiverId), message }
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
 * 检查是否是好友
 */
export function checkIsFriend(friendId) {
  return request({
    url: `/friend/${String(friendId)}/check`,
    method: 'get'
  })
}

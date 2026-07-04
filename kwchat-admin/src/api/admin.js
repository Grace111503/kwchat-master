import request from '@/utils/request'

// ========== 仪表盘 ==========

export function getDashboardStats() {
  return request({
    url: '/admin/dashboard',
    method: 'get'
  })
}

// ========== 系统配置 ==========

export function getAllConfigs() {
  return request({
    url: '/admin/config',
    method: 'get'
  })
}

export function getConfigsByGroup(group) {
  return request({
    url: `/admin/config/group/${group}`,
    method: 'get'
  })
}

export function saveConfig(data) {
  return request({
    url: '/admin/config',
    method: 'post',
    data
  })
}

export function updateConfig(key, value) {
  return request({
    url: `/admin/config/${key}`,
    method: 'put',
    data: { value }
  })
}

export function deleteConfig(key) {
  return request({
    url: `/admin/config/${key}`,
    method: 'delete'
  })
}

// ========== 日志管理 ==========

export function getOperationLogs(params) {
  return request({
    url: '/admin/log/operation',
    method: 'get',
    params
  })
}

export function clearOperationLogs() {
  return request({
    url: '/admin/log/operation',
    method: 'delete'
  })
}

export function getLoginLogs(params) {
  return request({
    url: '/admin/log/login',
    method: 'get',
    params
  })
}

export function clearLoginLogs() {
  return request({
    url: '/admin/log/login',
    method: 'delete'
  })
}

// ========== AI模型配置 ==========

export function getAllAiModelConfigs() {
  return request({
    url: '/admin/ai-model',
    method: 'get'
  })
}

export function getEnabledAiModelConfigs() {
  return request({
    url: '/admin/ai-model/enabled',
    method: 'get'
  })
}

export function getDefaultAiModel() {
  return request({
    url: '/admin/ai-model/default',
    method: 'get'
  })
}

export function saveAiModelConfig(data) {
  return request({
    url: '/admin/ai-model',
    method: 'post',
    data
  })
}

export function updateAiModelConfig(id, data) {
  return request({
    url: `/admin/ai-model/${id}`,
    method: 'put',
    data
  })
}

export function deleteAiModelConfig(id) {
  return request({
    url: `/admin/ai-model/${id}`,
    method: 'delete'
  })
}

export function setDefaultAiModel(id) {
  return request({
    url: `/admin/ai-model/${id}/default`,
    method: 'put'
  })
}

export function testAiModelConnection(id) {
  return request({
    url: `/admin/ai-model/${id}/test`,
    method: 'post'
  })
}
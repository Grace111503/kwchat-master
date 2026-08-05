import request from '@/utils/request'
import { isCapacitor } from '@/utils/platform'

/**
 * 上传图片
 */
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传视频
 */
export function uploadVideo(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/video',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传语音
 */
export function uploadVoice(file) {
  console.log('[File] 上传语音:', file.name, file.type, file.size, '字节')
  const formData = new FormData()
  formData.append('file', file, file.name)
  return request({
    url: '/file/voice',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传文件
 */
export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/document',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传头像
 */
export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除文件
 */
export function deleteFile(filePath) {
  return request({
    url: '/file',
    method: 'delete',
    params: { filePath }
  })
}

/**
 * 获取文件URL
 */
export function getFileUrl(filePath) {
  return request({
    url: '/file/url',
    method: 'get',
    params: { filePath }
  })
}

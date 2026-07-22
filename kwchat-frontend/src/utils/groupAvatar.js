/**
 * 群聊头像生成工具
 * 根据群成员头像自动生成拼接头像
 */

// 头像缓存
const avatarCache = new Map()

/**
 * 生成群聊头像
 * @param {Array} avatars - 头像URL数组（最多9个）
 * @param {Number} size - 画布大小（默认200）
 * @returns {Promise<String>} - Base64图片数据
 */
export async function generateGroupAvatar(avatars = [], size = 200) {
  // 生成缓存key
  const cacheKey = avatars.sort().join(',') + '_' + size
  if (avatarCache.has(cacheKey)) {
    return avatarCache.get(cacheKey)
  }

  // 如果没有头像或只有1个，返回空
  if (!avatars || avatars.length === 0) {
    return generateDefaultAvatar(size)
  }

  if (avatars.length === 1) {
    return await generateSingleAvatar(avatars[0], size)
  }

  // 加载所有头像
  const images = await loadImages(avatars)

  // 创建画布
  const canvas = document.createElement('canvas')
  canvas.width = size
  canvas.height = size
  const ctx = canvas.getContext('2d')

  // 填充白色背景
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, size, size)

  // 根据头像数量确定网格
  const count = Math.min(images.length, 9)
  let gridSize, gap, cellSize

  if (count <= 4) {
    // 2x2 四宫格
    gridSize = 2
    gap = 4
    cellSize = (size - gap * (gridSize + 1)) / gridSize
  } else {
    // 3x3 九宫格
    gridSize = 3
    gap = 3
    cellSize = (size - gap * (gridSize + 1)) / gridSize
  }

  // 绘制每个头像
  for (let i = 0; i < count; i++) {
    const row = Math.floor(i / gridSize)
    const col = i % gridSize
    const x = gap + col * (cellSize + gap)
    const y = gap + row * (cellSize + gap)

    await drawSquareImage(ctx, images[i], x, y, cellSize)
  }

  // 导出图片
  const result = canvas.toDataURL('image/png')
  avatarCache.set(cacheKey, result)

  return result
}

/**
 * 生成单个头像
 */
async function generateSingleAvatar(avatarUrl, size) {
  try {
    const img = await loadImage(avatarUrl)
    const canvas = document.createElement('canvas')
    canvas.width = size
    canvas.height = size
    const ctx = canvas.getContext('2d')

    // 绘制正方形头像
    await drawSquareImage(ctx, img, 0, 0, size)

    return canvas.toDataURL('image/png')
  } catch (e) {
    console.error('生成单个头像失败:', e)
    return generateDefaultAvatar(size)
  }
}

/**
 * 生成默认头像
 */
function generateDefaultAvatar(size) {
  const canvas = document.createElement('canvas')
  canvas.width = size
  canvas.height = size
  const ctx = canvas.getContext('2d')

  // 蓝色背景（正方形）
  ctx.fillStyle = '#409eff'
  ctx.fillRect(0, 0, size, size)

  // 白色文字
  ctx.fillStyle = '#ffffff'
  ctx.font = `bold ${size * 0.4}px Arial`
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText('群', size / 2, size / 2)

  return canvas.toDataURL('image/png')
}

/**
 * 加载单张图片
 */
function loadImage(url) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => resolve(img)
    img.onerror = () => reject(new Error('图片加载失败: ' + url))
    img.src = url
  })
}

/**
 * 批量加载图片
 */
async function loadImages(urls) {
  const images = []
  const promises = urls.slice(0, 9).map(async (url) => {
    try {
      const img = await loadImage(url)
      images.push(img)
    } catch (e) {
      console.warn('加载头像失败:', url)
    }
  })
  await Promise.all(promises)
  return images
}

/**
 * 绘制正方形图片
 */
async function drawSquareImage(ctx, img, x, y, size) {
  ctx.save()

  // 计算图片绘制区域（保持正方形，居中裁剪）
  const imgRatio = img.width / img.height
  let drawWidth, drawHeight, drawX, drawY

  if (imgRatio > 1) {
    // 宽图 - 裁剪左右
    drawHeight = size
    drawWidth = size * imgRatio
    drawX = x - (drawWidth - size) / 2
    drawY = y
  } else {
    // 高图或正方形 - 裁剪上下
    drawWidth = size
    drawHeight = size / imgRatio
    drawX = x
    drawY = y - (drawHeight - size) / 2
  }

  ctx.drawImage(img, drawX, drawY, drawWidth, drawHeight)
  ctx.restore()
}

/**
 * 清除缓存
 */
export function clearAvatarCache() {
  avatarCache.clear()
}

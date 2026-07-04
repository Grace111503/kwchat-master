<template>
  <div class="emoji-panel" v-show="visible">
    <!-- 表情分类标签 -->
    <div class="emoji-tabs">
      <div
        v-for="(category, index) in categories"
        :key="index"
        class="tab-item"
        :class="{ active: activeCategory === index }"
        @click="activeCategory = index"
      >
        {{ category.name }}
      </div>
    </div>

    <!-- 表情列表 -->
    <div class="emoji-content">
      <!-- 系统表情 -->
      <div v-if="activeCategory === 0" class="emoji-grid">
        <span
          v-for="emoji in systemEmojis"
          :key="emoji"
          class="emoji-item"
          @click="selectEmoji(emoji)"
          :title="emoji"
        >
          {{ emoji }}
        </span>
      </div>

      <!-- 自定义表情包 -->
      <div v-else class="emoji-grid custom-emojis">
        <div
          v-for="emoji in currentCustomEmojis"
          :key="emoji.id"
          class="emoji-item custom"
          @click="selectCustomEmoji(emoji)"
        >
          <img :src="emoji.url" :alt="emoji.name" loading="lazy" />
          <span class="emoji-name">{{ emoji.name }}</span>
        </div>

        <!-- 添加表情按钮 -->
        <div class="emoji-item add-btn" @click="triggerAddEmoji">
          <el-icon :size="24"><Plus /></el-icon>
        </div>
      </div>
    </div>

    <!-- 最近使用 -->
    <div class="recent-emojis" v-if="recentEmojis.length > 0 && activeCategory === 0">
      <div class="recent-header">
        <span>最近使用</span>
        <el-link type="primary" underline="never" @click="clearRecent">清空</el-link>
      </div>
      <div class="recent-list">
        <span
          v-for="emoji in recentEmojis"
          :key="emoji"
          class="emoji-item"
          @click="selectEmoji(emoji)"
        >
          {{ emoji }}
        </span>
      </div>
    </div>

    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      multiple
      style="display: none"
      @change="handleAddEmoji"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'select-custom'])

const fileInputRef = ref(null)
const activeCategory = ref(0)

// 表情分类
const categories = [
  { name: '😀', type: 'system' },
  { name: '🐱', type: 'animal' },
  { name: '🍕', type: 'food' },
  { name: '⚽', type: 'activity' },
  { name: '❤️', type: 'custom' }
]

// 系统表情
const systemEmojis = [
  '😀', '😁', '😂', '🤣', '😃', '😄', '😅', '😆', '😉', '😊',
  '😋', '😎', '😍', '🥰', '😘', '😗', '😙', '😚', '🙂', '🤗',
  '🤩', '🤔', '🤨', '😐', '😑', '😶', '🙄', '😏', '😣', '😥',
  '😮', '🤐', '😯', '😪', '😫', '🥱', '😴', '😌', '😛', '😜',
  '😝', '🤤', '😒', '😓', '😔', '😕', '🙃', '🤑', '😲', '🙁',
  '😖', '😞', '😟', '😤', '😢', '😭', '😦', '😧', '😨', '😩',
  '🤯', '😬', '😰', '😱', '🥵', '🥶', '😳', '🤪', '😵', '🥴',
  '😡', '😠', '🤬', '😈', '👿', '💀', '☠️', '💩', '🤡', '👹',
  '👻', '👽', '👾', '🤖', '🎃', '😺', '😸', '😹', '😻', '😼',
  '👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙', '👋', '🤚',
  '👏', '🙌', '🤝', '🙏', '✍️', '💪', '🦾', '🦿', '🦵', '🦶',
  '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔',
  '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮️'
]

// 自定义表情包
const customEmojis = ref([
  // 示例数据，实际应从服务器加载
  {
    id: 1,
    name: '开心',
    url: 'https://via.placeholder.com/50',
    category: 'default'
  }
])

// 最近使用的表情
const recentEmojis = ref([])

// 当前分类的自定义表情
const currentCustomEmojis = computed(() => {
  const categoryMap = {
    1: 'animal',
    2: 'food',
    3: 'activity',
    4: 'default'
  }
  const category = categoryMap[activeCategory.value] || 'default'
  return customEmojis.value.filter(e => e.category === category)
})

// 选择系统表情
const selectEmoji = (emoji) => {
  emit('select', emoji)
  addToRecent(emoji)
}

// 选择自定义表情
const selectCustomEmoji = (emoji) => {
  emit('select-custom', emoji)
}

// 添加到最近使用
const addToRecent = (emoji) => {
  const index = recentEmojis.value.indexOf(emoji)
  if (index > -1) {
    recentEmojis.value.splice(index, 1)
  }
  recentEmojis.value.unshift(emoji)
  if (recentEmojis.value.length > 20) {
    recentEmojis.value.pop()
  }
  // 保存到本地存储
  localStorage.setItem('recent-emojis', JSON.stringify(recentEmojis.value))
}

// 清空最近使用
const clearRecent = () => {
  recentEmojis.value = []
  localStorage.removeItem('recent-emojis')
}

// 触发添加表情
const triggerAddEmoji = () => {
  fileInputRef.value?.click()
}

// 添加自定义表情
const handleAddEmoji = (event) => {
  const files = event.target.files
  if (!files.length) return

  for (const file of files) {
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      continue
    }

    if (file.size > 1 * 1024 * 1024) {
      ElMessage.error('表情图片大小不能超过1MB')
      continue
    }

    // 创建本地预览URL
    const url = URL.createObjectURL(file)
    const emoji = {
      id: Date.now() + Math.random(),
      name: file.name.replace(/\.[^.]+$/, ''),
      url,
      category: 'default',
      file
    }

    customEmojis.value.push(emoji)
  }

  // 保存到本地存储
  saveCustomEmojis()

  // 清空input
  event.target.value = ''
}

// 保存自定义表情到本地存储
const saveCustomEmojis = () => {
  const emojisToSave = customEmojis.value.filter(e => !e.file)
  localStorage.setItem('custom-emojis', JSON.stringify(emojisToSave))
}

// 加载自定义表情
const loadCustomEmojis = () => {
  try {
    const saved = localStorage.getItem('custom-emojis')
    if (saved) {
      customEmojis.value = [...customEmojis.value, ...JSON.parse(saved)]
    }
  } catch (e) {
    console.error('加载自定义表情失败:', e)
  }
}

// 加载最近使用的表情
const loadRecentEmojis = () => {
  try {
    const saved = localStorage.getItem('recent-emojis')
    if (saved) {
      recentEmojis.value = JSON.parse(saved)
    }
  } catch (e) {
    console.error('加载最近表情失败:', e)
  }
}

// 初始化
loadCustomEmojis()
loadRecentEmojis()
</script>

<style lang="scss" scoped>
.emoji-panel {
  position: absolute;
  bottom: 100%;
  left: 0;
  width: 360px;
  max-height: 400px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  z-index: 100;
}

.emoji-tabs {
  display: flex;
  border-bottom: 1px solid #e8e8e8;
  padding: 8px;
  gap: 8px;

  .tab-item {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    cursor: pointer;
    border-radius: 8px;
    transition: background 0.2s;

    &:hover {
      background: #f5f5f5;
    }

    &.active {
      background: #e8f4ff;
    }
  }
}

.emoji-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
}

.emoji-item {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    background: #f5f5f5;
    transform: scale(1.1);
  }

  &.custom {
    flex-direction: column;
    width: 60px;
    height: 60px;

    img {
      width: 40px;
      height: 40px;
      object-fit: contain;
    }

    .emoji-name {
      font-size: 10px;
      color: #999;
      margin-top: 2px;
      max-width: 56px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  &.add-btn {
    border: 2px dashed #ddd;
    color: #999;

    &:hover {
      border-color: #409eff;
      color: #409eff;
    }
  }
}

.recent-emojis {
  border-top: 1px solid #e8e8e8;
  padding: 8px 12px;

  .recent-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    span {
      font-size: 12px;
      color: #999;
    }
  }

  .recent-list {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
  }
}
</style>
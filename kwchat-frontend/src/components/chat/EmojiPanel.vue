<template>
  <div class="emoji-panel" v-show="visible">
    <!-- 表情分类标签 -->
    <div class="emoji-tabs">
      <div
        v-for="(category, index) in categories"
        :key="index"
        class="tab-item"
        :class="{ active: activeCategory === index }"
        :title="category.label"
        @click="activeCategory = index"
      >
        <el-icon v-if="category.icon" :size="18"><component :is="category.icon" /></el-icon>
        <span v-else>{{ category.name }}</span>
      </div>
    </div>

    <!-- 表情列表 -->
    <div class="emoji-content">
      <!-- 最近使用 -->
      <div v-if="activeCategory === 0" class="emoji-grid">
        <div v-if="recentEmojis.length === 0" class="empty-tip">暂无最近使用的表情</div>
        <span
          v-for="emoji in recentEmojis"
          :key="`recent-${emoji}`"
          class="emoji-item"
          @click="selectEmoji(emoji)"
          :title="emoji"
        >
          {{ emoji }}
        </span>
      </div>

      <!-- 分类表情 -->
      <div v-else-if="activeCategory < categories.length - 1" class="emoji-grid">
        <span
          v-for="emoji in currentCategoryEmojis"
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
          v-for="emoji in customEmojis"
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

    <!-- 最近使用快捷区（仅在非“最近”分类显示） -->
    <div class="recent-emojis" v-if="recentEmojis.length > 0 && activeCategory !== 0">
      <div class="recent-header">
        <span>最近使用</span>
        <el-link type="primary" underline="never" @click="clearRecent">清空</el-link>
      </div>
      <div class="recent-list">
        <span
          v-for="emoji in recentEmojis.slice(0, 12)"
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
import { Clock, Plus } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'select-custom'])

const fileInputRef = ref(null)
const activeCategory = ref(0)

// 表情分类（0=最近，1=笑脸，2=手势，3=动物，4=食物，5=活动，6=符号，7=自定义）
const categories = [
  { name: '', label: '最近使用', icon: Clock },
  { name: '😀', label: '笑脸' },
  { name: '👍', label: '手势' },
  { name: '🐱', label: '动物' },
  { name: '🍕', label: '食物' },
  { name: '⚽', label: '活动' },
  { name: '❤️', label: '符号' },
  { name: '⭐', label: '自定义' }
]

// 笑脸表情
const smileEmojis = [
  '😀', '😁', '😂', '🤣', '😃', '😄', '😅', '😆', '😉', '😊',
  '😋', '😎', '😍', '🥰', '😘', '😗', '😙', '😚', '🙂', '🤗',
  '🤩', '🤔', '🤨', '😐', '😑', '😶', '🙄', '😏', '😣', '😥',
  '😮', '🤐', '😯', '😪', '😫', '🥱', '😴', '😌', '😛', '😜',
  '😝', '🤤', '😒', '😓', '😔', '😕', '🙃', '🤑', '😲', '🙁',
  '😖', '😞', '😟', '😤', '😢', '😭', '😦', '😧', '😨', '😩',
  '🤯', '😬', '😰', '😱', '🥵', '🥶', '😳', '🤪', '😵', '🥴',
  '😡', '😠', '🤬', '😈', '👿', '💀', '☠️', '💩', '🤡', '👹'
]

// 手势表情
const gestureEmojis = [
  '👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙', '👋', '🤚',
  '👏', '🙌', '🤝', '🙏', '✍️', '💪', '🦾', '🦿', '🦵', '🦶',
  '👊', '✊', '🤛', '🤜', '🤞', '✋', '🖐️', '🖖', '👈', '👉',
  '👆', '👇', '☝️', '🫵', '🤌', '🫰', '🤌', '🫴', '🫳', '💅'
]

// 动物表情
const animalEmojis = [
  '🐱', '🐶', '🐭', '🐹', '🐰', '🦊', '🐻', '🐼', '🐨', '🐯',
  '🦁', '🐮', '🐷', '🐸', '🐵', '🙈', '🙉', '🙊', '🐒', '🐔',
  '🐧', '🐦', '🐤', '🐣', '🐥', '🦆', '🦅', '🦉', '🦇', '🐺',
  '🐗', '🐴', '🦄', '🐝', '🐛', '🦋', '🐌', '🐞', '🐜', '🦗',
  '🕷️', '🦂', '🐢', '🐍', '🦎', '🦖', '🦕', '🐙', '🦑', '🦐',
  '🦞', '🦀', '🐡', '🐠', '🐟', '🐬', '🐳', '🐋', '🦈', '🐊'
]

// 食物表情
const foodEmojis = [
  '🍕', '🍔', '🍟', '🌭', '🥪', '🌮', '🌯', '🥙', '🧆', '🥚',
  '🍳', '🥘', '🍲', '🥣', '🥗', '🍿', '🧈', '🧂', '🥫', '🍱',
  '🍘', '🍙', '🍚', '🍛', '🍜', '🍝', '🍠', '🍢', '🍣', '🍤',
  '🍥', '🥮', '🍡', '🥟', '🥠', '🥡', '🍦', '🍧', '🍨', '🍩',
  '🍪', '🎂', '🍰', '🧁', '🥧', '🍫', '🍬', '🍭', '🍮', '🍯',
  '🍎', '🍐', '🍊', '🍋', '🍌', '🍉', '🍇', '🍓', '🍈', '🍒'
]

// 活动表情
const activityEmojis = [
  '⚽', '🏀', '🏈', '⚾', '🥎', '🎾', '🏐', '🏉', '🥏', '🎱',
  '🪀', '🏓', '🏸', '🏒', '🏑', '🥍', '🏏', '🪃', '🥅', '⛳',
  '🪁', '🏹', '🎣', '🤿', '🥊', '🥋', '🎽', '🛹', '🛼', '🛷',
  '⛸️', '🥌', '🎿', '⛷️', '🏂', '🪂', '🏋️', '🤼', '🤸', '⛹️',
  '🤺', '🤾', '🏌️', '🏇', '🧘', '🏄', '🏊', '🤽', '🚣', '🧗'
]

// 符号表情
const symbolEmojis = [
  '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔',
  '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '☮️',
  '✨', '⭐', '🌟', '💫', '✅', '❌', '❓', '❗', '⚠️', '🔴',
  '🟠', '🟡', '🟢', '🔵', '🟣', '🟤', '⚫', '⚪', '🔶', '🔷',
  '🏆', '🥇', '🥈', '🥉', '🎁', '🎉', '🎊', '🎈', '🎂', '🎄',
  '💯', '🔔', '🔕', '🎵', '🎶', '〰️', '➰', '✔️', '✖️', '➕'
]

// 当前分类的表情数据
const currentCategoryEmojis = computed(() => {
  const map = [
    smileEmojis,
    gestureEmojis,
    animalEmojis,
    foodEmojis,
    activityEmojis,
    symbolEmojis
  ]
  // activeCategory === 0 是最近使用，由模板单独处理
  const idx = activeCategory.value - 1
  return map[idx] || smileEmojis
})

// 自定义表情包
const customEmojis = ref([
  {
    id: 1,
    name: '开心',
    url: 'https://via.placeholder.com/50',
    category: 'default'
  }
])

// 最近使用的表情
const recentEmojis = ref([])

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

  saveCustomEmojis()
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
  gap: 4px;
  overflow-x: auto;
  scrollbar-width: none;
  &::-webkit-scrollbar { display: none; }

  .tab-item {
    min-width: 36px;
    height: 36px;
    padding: 0 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    cursor: pointer;
    border-radius: 8px;
    transition: background 0.2s;
    flex-shrink: 0;

    &:hover {
      background: #f5f5f5;
    }

    &.active {
      background: #e8f4ff;
      color: #2b7fff;
    }
  }
}

.empty-tip {
  grid-column: 1 / -1;
  text-align: center;
  color: #999;
  font-size: 13px;
  padding: 40px 0;
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

// 移动端适配
@media (max-width: 768px) {
  .emoji-panel {
    width: calc(100vw - 32px);
    max-width: 360px;
    left: -8px;
  }
}
</style>
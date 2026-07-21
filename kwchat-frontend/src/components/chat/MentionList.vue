<template>
  <div class="mention-list" v-show="visible">
    <div class="mention-header">
      <span>@提醒谁看</span>
    </div>

    <div class="mention-search">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索成员"
        size="small"
        clearable
      />
    </div>

    <div class="mention-content">
      <!-- @所有人 -->
      <div
        class="mention-item"
        v-if="showAll && !searchKeyword"
        @click="selectMention({ id: 'all', nickname: '所有人' })"
      >
        <div class="mention-avatar all">
          <el-icon><User /></el-icon>
        </div>
        <div class="mention-info">
          <div class="mention-name">所有人</div>
        </div>
      </div>

      <!-- 成员列表 -->
      <div
        v-for="member in filteredMembers"
        :key="member.userId || member.id"
        class="mention-item"
        @click="selectMention(member)"
      >
        <el-avatar :size="32" :src="member.avatar">
          {{ getAvatarFallback(member.nickname) }}
        </el-avatar>
        <div class="mention-info">
          <div class="mention-name">{{ member.nickname }}</div>
          <div class="mention-role" v-if="member.role === 2">群主</div>
          <div class="mention-role admin" v-else-if="member.role === 1">管理员</div>
        </div>
        <div class="mention-online" v-if="member.onlineStatus === 1">
          <span class="online-dot"></span>
          <span class="online-text">在线</span>
        </div>
      </div>

      <!-- 无结果 -->
      <div class="mention-empty" v-if="filteredMembers.length === 0">
        未找到匹配的成员
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { User } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  members: {
    type: Array,
    default: () => []
  },
  showAll: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['select'])

const searchKeyword = ref('')

// 获取头像 fallback 文字（显示最后两个字）
const getAvatarFallback = (name) => {
  if (!name) return '用户'
  return name.length >= 2 ? name.slice(-2) : name
}

// 过滤后的成员列表
const filteredMembers = computed(() => {
  if (!searchKeyword.value) {
    return props.members
  }

  const keyword = searchKeyword.value.toLowerCase()
  return props.members.filter(member =>
    (member.nickname || member.username)?.toLowerCase().includes(keyword) ||
    member.username?.toLowerCase().includes(keyword)
  )
})

// 选择@对象
const selectMention = (member) => {
  emit('select', member)
  searchKeyword.value = ''
}
</script>

<style lang="scss" scoped>
.mention-list {
  position: absolute;
  bottom: 100%;
  left: 50px;
  width: 280px;
  max-height: 350px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  z-index: 100;
}

.mention-header {
  padding: 8px 12px;
  border-bottom: 1px solid #e8e8e8;

  span {
    font-size: 12px;
    color: #999;
  }
}

.mention-search {
  padding: 8px 12px;
  border-bottom: 1px solid #e8e8e8;
}

.mention-content {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.mention-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: #f5f5f5;
  }
}

.mention-avatar {
  &.all {
    width: 32px;
    height: 32px;
    background: #409eff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    .el-icon {
      color: #fff;
      font-size: 16px;
    }
  }
}

.mention-info {
  flex: 1;
  min-width: 0;

  .mention-name {
    font-size: 14px;
    color: #333;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .mention-role {
    font-size: 12px;
    color: #e6a23c;

    &.admin {
      color: #409eff;
    }
  }
}

.mention-online {
  display: flex;
  align-items: center;
  gap: 4px;

  .online-dot {
    width: 6px;
    height: 6px;
    background: #67c23a;
    border-radius: 50%;
  }

  .online-text {
    font-size: 12px;
    color: #67c23a;
  }
}

.mention-empty {
  padding: 24px;
  text-align: center;
  font-size: 13px;
  color: #999;
}
</style>
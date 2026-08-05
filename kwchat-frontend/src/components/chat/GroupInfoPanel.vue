<template>
  <el-drawer
    v-model="visible"
    title="群聊信息"
    direction="rtl"
    :size="windowWidth <= 768 ? '100%' : '360px'"
    @open="loadGroupInfo"
  >
    <div class="group-info-panel" v-loading="loading">
      <!-- 群基本信息 -->
      <div class="group-header">
        <div class="avatar-upload" v-if="isOwnerOrAdmin" @click="triggerAvatarUpload">
          <el-avatar :size="64" :src="getFullFileUrl(conversation?.avatar)" shape="square">
            {{ getAvatarFallback(conversation?.name) }}
          </el-avatar>
          <div class="avatar-overlay">
            <el-icon><Camera /></el-icon>
            <span>更换头像</span>
          </div>
        </div>
        <el-avatar v-else :size="64" :src="getFullFileUrl(conversation?.avatar)" shape="square">
          {{ getAvatarFallback(conversation?.name) }}
        </el-avatar>
        <div class="group-name" v-if="!editingName">{{ conversation?.name }}</div>
        <div class="group-name-edit" v-else>
          <el-input
            v-model="editName"
            size="small"
            maxlength="50"
            show-word-limit
            @keyup.enter="saveGroupName"
            @blur="saveGroupName"
          />
        </div>
        <div class="group-name-action" v-if="isOwnerOrAdmin && !editingName">
          <el-button type="primary" link size="small" @click="startEditName">
            <el-icon><Edit /></el-icon>
          </el-button>
        </div>
        <div class="group-member-count">{{ members.length }}人</div>
      </div>

      <!-- 群公告 -->
      <div class="info-section">
        <div class="section-title">群公告</div>
        <GroupAnnouncement
          :conversation-id="conversationId"
          :announcement="conversation?.announcement || ''"
          :announcement-time="conversation?.updateTime"
          :can-edit="isOwnerOrAdmin"
          @save="handleSaveAnnouncement"
        />
      </div>

      <!-- 群成员列表 -->
      <div class="info-section">
        <div class="section-header">
          <span class="section-title">群成员（{{ members.length }}）</span>
          <el-button type="primary" link @click="showAddMember = true">
            <el-icon><Plus /></el-icon>
            添加
          </el-button>
        </div>
        <div class="member-list">
          <div
            v-for="member in members"
            :key="member.userId"
            class="member-item"
          >
            <el-avatar :size="36" :src="getFullFileUrl(member.avatar)" shape="square">
              {{ getAvatarFallback(member.nickname || member.username) }}
            </el-avatar>
            <div class="member-info">
              <div class="member-name">
                {{ member.nickname || member.username }}
                <el-tag v-if="member.role === 2" size="small" type="warning">群主</el-tag>
                <el-tag v-else-if="member.role === 1" size="small" type="info">管理员</el-tag>
              </div>
            </div>
            <div class="member-actions" v-if="isOwner && !isCurrentUser(member)">
              <el-dropdown trigger="click" v-if="member.role === 0">
                <el-button type="primary" link size="small">
                  设置管理员
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="setAdmin(member)">设为管理员</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button
                v-else-if="member.role === 1"
                type="warning"
                link
                size="small"
                @click="cancelAdmin(member)"
              >
                取消管理员
              </el-button>
            </div>
            <el-button
              v-if="canRemoveMember(member)"
              type="danger"
              link
              size="small"
              @click="removeMember(member)"
            >
              移除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="info-actions">
        <el-button type="danger" plain @click="leaveGroup" v-if="!isOwner">退出群聊</el-button>
        <el-button type="danger" @click="handleDissolveGroup" v-if="isOwner">解散群聊</el-button>
      </div>
    </div>

    <!-- 添加成员对话框 -->
    <el-dialog v-model="showAddMember" title="添加成员" width="min(400px, 90vw)" append-to-body>
      <div class="add-member-list">
        <div
          v-for="friend in availableFriends"
          :key="friend.id"
          class="friend-add-item"
          :class="{ selected: selectedNewMembers.includes(friend.id) }"
          @click="toggleNewMember(friend.id)"
        >
          <el-avatar :size="32" :src="friend.avatar" shape="square">
            {{ getAvatarFallback(friend.nickname) }}
          </el-avatar>
          <span class="friend-add-name">{{ friend.remark || friend.nickname }}</span>
          <el-icon v-if="selectedNewMembers.includes(friend.id)" class="check-icon"><Check /></el-icon>
        </div>
        <div v-if="availableFriends.length === 0" class="empty-friends">
          暂无可添加的好友
        </div>
      </div>
      <template #footer>
        <el-button @click="showAddMember = false">取消</el-button>
        <el-button type="primary" :loading="addMemberLoading" @click="handleAddMembers">确定</el-button>
      </template>
    </el-dialog>

    <!-- 隐藏的文件输入 -->
    <input
      ref="avatarInputRef"
      type="file"
      accept="image/*"
      style="display: none"
      @change="handleAvatarUpload"
    />
  </el-drawer>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Check, Edit, Camera } from '@element-plus/icons-vue'
import { useChatStore } from '@/store/chat'
import { useUserStore } from '@/store/user'
import { getConversationMembers, addConversationMember, removeConversationMember, updateAnnouncement, updateGroupName, updateGroupAvatar, dissolveGroup, updateMemberRole } from '@/api/conversation'
import { getFriendList } from '@/api/friend'
import { uploadImage } from '@/api/file'
import { getFullFileUrl } from '@/utils/platform'
import GroupAnnouncement from './GroupAnnouncement.vue'

const props = defineProps({
  conversationId: { type: [Number, String], default: null }
})

const visible = defineModel('visible', { type: Boolean, default: false })

const chatStore = useChatStore()
const userStore = useUserStore()

const windowWidth = ref(window.innerWidth)
window.addEventListener('resize', () => {
  windowWidth.value = window.innerWidth
})

const loading = ref(false)
const members = ref([])
const friends = ref([])
const showAddMember = ref(false)
const selectedNewMembers = ref([])
const addMemberLoading = ref(false)
const editingName = ref(false)
const editName = ref('')
const avatarInputRef = ref(null)

const conversation = computed(() => {
  if (!props.conversationId) return null
  return chatStore.conversations.find(c => c.id === props.conversationId)
})

const availableFriends = computed(() => {
  const memberIds = new Set(members.value.map(m => m.userId))
  return friends.value.filter(f => !memberIds.has(f.id))
})

const isOwnerOrAdmin = computed(() => {
  const currentUserId = userStore.userInfo?.id
  const currentMember = members.value.find(m => m.userId === currentUserId)
  return currentMember?.role === 2 || currentMember?.role === 1
})

const isOwner = computed(() => {
  const currentUserId = userStore.userInfo?.id
  const currentMember = members.value.find(m => m.userId === currentUserId)
  return currentMember?.role === 2
})

const canRemoveMember = (member) => {
  const currentUserId = userStore.userInfo?.id
  const currentMember = members.value.find(m => m.userId === currentUserId)
  // 群主和管理员可以移除普通成员，不能移除自己
  if (member.userId === currentUserId) return false
  if (currentMember?.role === 2) return true // 群主
  if (currentMember?.role === 1 && member.role === 0) return true // 管理员移除普通成员
  return false
}

const isCurrentUser = (member) => {
  return member.userId === userStore.userInfo?.id
}

const setAdmin = async (member) => {
  try {
    await updateMemberRole(props.conversationId, member.userId, 1)
    member.role = 1
    ElMessage.success('已设为管理员')
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

const cancelAdmin = async (member) => {
  try {
    await ElMessageBox.confirm(`确定要取消 ${member.nickname || member.username} 的管理员身份吗？`, '取消管理员', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateMemberRole(props.conversationId, member.userId, 0)
    member.role = 0
    ElMessage.success('已取消管理员')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const getAvatarFallback = (name) => {
  if (!name) return '用户'
  return name.length >= 2 ? name.slice(-2) : name
}

const loadGroupInfo = async () => {
  if (!props.conversationId) return
  loading.value = true
  try {
    const [membersRes, friendsRes] = await Promise.all([
      getConversationMembers(props.conversationId),
      getFriendList()
    ])
    if (membersRes.code === 200) members.value = membersRes.data || []
    if (friendsRes.code === 200) friends.value = friendsRes.data || []
  } catch (error) {
    console.error('加载群聊信息失败:', error)
  } finally {
    loading.value = false
  }
}

const toggleNewMember = (friendId) => {
  const index = selectedNewMembers.value.indexOf(friendId)
  if (index === -1) {
    selectedNewMembers.value.push(friendId)
  } else {
    selectedNewMembers.value.splice(index, 1)
  }
}

const handleAddMembers = async () => {
  if (selectedNewMembers.value.length === 0) {
    ElMessage.warning('请选择要添加的成员')
    return
  }
  addMemberLoading.value = true
  try {
    for (const userId of selectedNewMembers.value) {
      await addConversationMember(props.conversationId, userId)
    }
    ElMessage.success('成员添加成功')
    showAddMember.value = false
    selectedNewMembers.value = []
    await loadGroupInfo()
    // 更新会话成员数
    if (conversation.value) {
      conversation.value.memberCount = members.value.length
    }
  } catch (error) {
    ElMessage.error('添加成员失败')
  } finally {
    addMemberLoading.value = false
  }
}

const removeMember = async (member) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除 ${member.nickname || member.username} 吗？`,
      '移除成员',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await removeConversationMember(props.conversationId, member.userId)
    ElMessage.success('成员已移除')
    await loadGroupInfo()
    if (conversation.value) {
      conversation.value.memberCount = members.value.length
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除成员失败')
    }
  }
}

const leaveGroup = async () => {
  try {
    await ElMessageBox.confirm('确定要退出该群聊吗？', '退出群聊', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const currentUserId = userStore.userInfo?.id
    await removeConversationMember(props.conversationId, currentUserId)
    ElMessage.success('已退出群聊')
    visible.value = false
    // 从会话列表中移除
    const index = chatStore.conversations.findIndex(c => c.id === props.conversationId)
    if (index !== -1) chatStore.conversations.splice(index, 1)
    // 清除当前会话
    if (chatStore.currentConversation?.id === props.conversationId) {
      chatStore.currentConversation = null
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('退出群聊失败')
    }
  }
}

const handleSaveAnnouncement = async (announcement) => {
  try {
    await updateAnnouncement(props.conversationId, announcement)
    // 更新本地会话数据
    if (conversation.value) {
      conversation.value.announcement = announcement
    }
    ElMessage.success('公告发布成功')
  } catch (error) {
    ElMessage.error('公告发布失败')
  }
}

const handleDissolveGroup = async () => {
  try {
    await ElMessageBox.confirm('确定要解散该群聊吗？此操作不可恢复！', '解散群聊', {
      confirmButtonText: '确定解散',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await dissolveGroup(props.conversationId)
    ElMessage.success('群聊已解散')
    visible.value = false
    // 从会话列表中移除
    const index = chatStore.conversations.findIndex(c => c.id === props.conversationId)
    if (index !== -1) chatStore.conversations.splice(index, 1)
    // 清除当前会话
    if (chatStore.currentConversation?.id === props.conversationId) {
      chatStore.currentConversation = null
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('解散群聊失败')
    }
  }
}

const startEditName = () => {
  editName.value = conversation.value?.name || ''
  editingName.value = true
}

const saveGroupName = async () => {
  if (!editName.value.trim()) {
    ElMessage.warning('群名称不能为空')
    return
  }
  if (editName.value.trim() === conversation.value?.name) {
    editingName.value = false
    return
  }
  try {
    await updateGroupName(props.conversationId, editName.value.trim())
    // 更新本地会话数据
    if (conversation.value) {
      conversation.value.name = editName.value.trim()
    }
    // 更新聊天 store 中的会话名称
    const conv = chatStore.conversations.find(c => c.id === props.conversationId)
    if (conv) conv.name = editName.value.trim()
    if (chatStore.currentConversation?.id === props.conversationId) {
      chatStore.currentConversation.name = editName.value.trim()
    }
    editingName.value = false
    ElMessage.success('群名称已修改')
  } catch (error) {
    ElMessage.error('修改群名称失败')
  }
}

const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请选择图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  try {
    const res = await uploadImage(file)
    if (res.code === 200) {
      const avatarUrl = res.data.url
      await updateGroupAvatar(props.conversationId, avatarUrl)
      // 更新本地数据
      if (conversation.value) {
        conversation.value.avatar = avatarUrl
      }
      const conv = chatStore.conversations.find(c => c.id === props.conversationId)
      if (conv) conv.avatar = avatarUrl
      if (chatStore.currentConversation?.id === props.conversationId) {
        chatStore.currentConversation.avatar = avatarUrl
      }
      ElMessage.success('头像修改成功')
    }
  } catch (error) {
    ElMessage.error('头像修改失败')
  } finally {
    event.target.value = ''
  }
}

watch(() => showAddMember.value, (val) => {
  if (val) selectedNewMembers.value = []
})
</script>

<style lang="scss" scoped>
.group-info-panel {
  padding: 0;
}

.group-header {
  text-align: center;
  padding: 20px;
  border-bottom: 1px solid #eee;

  .avatar-upload {
    position: relative;
    display: inline-block;
    cursor: pointer;

    &:hover .avatar-overlay {
      opacity: 1;
    }
  }

  .avatar-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transition: opacity 0.2s;
    border-radius: 4px;

    .el-icon {
      color: #fff;
      font-size: 20px;
      margin-bottom: 4px;
    }

    span {
      color: #fff;
      font-size: 12px;
    }
  }

  .group-name {
    font-size: 18px;
    font-weight: 600;
    color: #1a1a1a;
    margin: 12px 0 4px;
  }

  .group-name-edit {
    margin: 8px 0 4px;
  }

  .group-name-action {
    margin-bottom: 4px;
  }

  .group-member-count {
    font-size: 13px;
    color: #999;
  }
}

.info-section {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.announcement-content {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}

.member-list {
  max-height: 300px;
  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 8px 0;

  &:not(:last-child) {
    border-bottom: 1px solid #f5f5f5;
  }
}

.member-info {
  flex: 1;
  margin-left: 10px;
}

.member-name {
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}

.member-actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

.info-actions {
  padding: 20px;
  text-align: center;
}

.add-member-list {
  max-height: 300px;
  overflow-y: auto;
}

.friend-add-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.15s;

  &:hover {
    background: #f5f5f5;
  }

  &.selected {
    background: #e8f0fe;
  }
}

.friend-add-name {
  flex: 1;
  margin-left: 10px;
  font-size: 14px;
  color: #333;
}

.check-icon {
  color: #2b7fff;
}

.empty-friends {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 13px;
}
</style>

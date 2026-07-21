<template>
  <div class="contacts-container">
    <!-- 左侧联系人列表 -->
    <div class="contacts-sidebar">
      <div class="contacts-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索联系人"
          prefix-icon="Search"
          clearable
        />
        <el-button type="primary" :icon="Plus" @click="showAddFriend" title="添加好友" />
        <el-button type="success" :icon="Plus" @click="showCreateGroup" title="创建群聊" />
      </div>

      <div class="contacts-tabs">
        <div
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </div>
      </div>

      <div class="contacts-list">
        <template v-if="activeTab === 'friends'">
          <div
            v-for="friend in filteredFriends"
            :key="friend.id"
            class="contact-item"
            :class="{ active: selectedContact?.id === friend.id }"
            @click="selectContact(friend)"
          >
            <el-avatar :size="38" :src="friend.avatar" shape="square">
              {{ getAvatarFallback(friend.nickname) }}
            </el-avatar>
            <div class="contact-info">
              <div class="contact-name">{{ friend.remark || friend.nickname }}</div>
              <div class="contact-signature text-ellipsis">{{ friend.signature || '暂无签名' }}</div>
            </div>
          </div>
        </template>

        <template v-if="activeTab === 'groups'">
          <div
            v-for="group in filteredGroups"
            :key="group.id"
            class="contact-item"
            :class="{ active: selectedContact?.id === group.id }"
            @click="startGroupChat(group)"
          >
            <el-avatar :size="38" :src="group.avatar" shape="square">
              {{ getAvatarFallback(group.name) }}
            </el-avatar>
            <div class="contact-info">
              <div class="contact-name">{{ group.name }}</div>
              <div class="contact-signature text-ellipsis">{{ group.memberCount }}人</div>
            </div>
          </div>
        </template>

        <template v-if="activeTab === 'requests'">
          <div
            v-for="request in friendRequests"
            :key="request.id"
            class="contact-item request-item"
          >
            <el-avatar :size="38" :src="request.senderAvatar" shape="square">
              {{ getAvatarFallback(request.senderName) }}
            </el-avatar>
            <div class="contact-info">
              <div class="contact-name">{{ request.senderName }}</div>
              <div class="contact-signature">{{ request.message || '请求添加好友' }}</div>
            </div>
            <div class="request-actions">
              <el-button v-if="request.status === 0" type="primary" size="small" @click="handleRequest(request, 1)">同意</el-button>
              <el-button v-if="request.status === 0" size="small" @click="handleRequest(request, 2)">拒绝</el-button>
              <span v-if="request.status === 1" class="status-text agreed">已同意</span>
              <span v-if="request.status === 2" class="status-text rejected">已拒绝</span>
            </div>
          </div>
        </template>

        <template v-if="activeTab === 'blacklist'">
          <div
            v-for="user in blacklist"
            :key="user.id"
            class="contact-item"
          >
            <el-avatar :size="38" :src="user.avatar" shape="square">
              {{ getAvatarFallback(user.nickname) }}
            </el-avatar>
            <div class="contact-info">
              <div class="contact-name">{{ user.nickname || user.username }}</div>
              <div class="contact-signature">{{ user.username }}</div>
            </div>
            <el-button type="danger" size="small" link @click="handleUnblackFriend(user)">取消拉黑</el-button>
          </div>
          <div v-if="blacklist.length === 0" class="empty-tip">
            <span>暂无黑名单</span>
          </div>
        </template>
      </div>
    </div>

    <!-- 右侧详情 -->
    <div class="contact-detail">
      <template v-if="selectedContact">
        <div class="detail-header">
          <el-avatar :size="72" :src="selectedContact.avatar" shape="square">
            {{ getAvatarFallback(selectedContact.name || selectedContact.nickname) }}
          </el-avatar>
          <h2 class="detail-name">
            {{ selectedContact.remark || selectedContact.name || selectedContact.nickname }}
          </h2>
          <p class="detail-signature">{{ selectedContact.signature || '暂无签名' }}</p>
        </div>

        <div class="detail-info">
          <div class="info-item">
            <span class="info-label">用户名</span>
            <span class="info-value">{{ selectedContact.username || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">分组</span>
            <span class="info-value">
              <el-select
                v-model="selectedContact.groupName"
                placeholder="选择分组"
                size="small"
                allow-create
                filterable
                @change="handleGroupChange"
              >
                <el-option label="默认分组" value="" />
                <el-option v-for="group in friendGroups" :key="group" :label="group" :value="group" />
              </el-select>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">部门</span>
            <span class="info-value">{{ selectedContact.department || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">手机号</span>
            <span class="info-value">{{ selectedContact.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ selectedContact.email || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">性别</span>
            <span class="info-value">{{ getGenderText(selectedContact.gender) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">个性签名</span>
            <span class="info-value">{{ selectedContact.signature || '未设置' }}</span>
          </div>
        </div>

        <div class="detail-actions">
          <el-button type="primary" @click="startChat">
            <el-icon><ChatDotRound /></el-icon>
            发消息
          </el-button>
          <el-button @click="editRemark">
            <el-icon><Edit /></el-icon>
            修改备注
          </el-button>
          <el-button type="warning" plain @click="handleBlackFriend">
            <el-icon><Warning /></el-icon>
            拉黑
          </el-button>
          <el-button type="danger" plain @click="deleteFriendAction">
            <el-icon><Delete /></el-icon>
            删除好友
          </el-button>
        </div>
      </template>

      <template v-else>
        <div class="empty-detail">
          <p>选择联系人查看详情</p>
        </div>
      </template>
    </div>

    <!-- 添加好友对话框 -->
    <el-dialog v-model="addFriendVisible" title="添加好友" width="400px">
      <el-form :model="addFriendForm" label-width="80px">
        <el-form-item label="搜索">
          <el-input
            v-model="addFriendForm.keyword"
            placeholder="输入用户名/昵称/手机号/邮箱"
            clearable
          />
        </el-form-item>
      </el-form>

      <div v-loading="searchLoading">
        <div v-if="filteredSearchResults.length > 0" class="search-results">
          <div
            v-for="user in filteredSearchResults"
            :key="user.id"
            class="search-result-item"
          >
            <el-avatar :size="38" :src="user.avatar" shape="square">
              {{ getAvatarFallback(user.nickname) }}
            </el-avatar>
            <div class="result-info">
              <div class="result-name">{{ user.nickname }}</div>
              <div class="result-username">{{ user.username }}</div>
            </div>
            <el-button v-if="friendIdSet.has(user.id)" size="small" disabled>已添加</el-button>
            <el-button v-else type="primary" size="small" @click="sendFriendRequest(user)">添加</el-button>
          </div>
        </div>
        <div v-else-if="searched && !searchLoading && addFriendForm.keyword" class="empty-results">
          <p style="text-align:center;color:#bbb;padding:20px 0;">用户不存在</p>
        </div>
      </div>

      <template #footer>
        <el-button @click="addFriendVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 编辑备注对话框 -->
    <el-dialog v-model="editRemarkVisible" title="修改备注" width="400px">
      <el-form :model="editRemarkForm" label-width="80px">
        <el-form-item label="备注名">
          <el-input
            v-model="editRemarkForm.remark"
            placeholder="请输入备注名"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editRemarkVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRemark">保存</el-button>
      </template>
    </el-dialog>

    <!-- 创建群聊对话框 -->
    <el-dialog v-model="createGroupVisible" title="创建群聊" width="450px">
      <el-form :model="createGroupForm" label-width="80px">
        <el-form-item label="群聊名称">
          <el-input
            v-model="createGroupForm.name"
            placeholder="请输入群聊名称"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="选择成员">
          <div class="group-member-select">
            <div class="selected-members" v-if="createGroupForm.memberIds.length > 0">
              <el-tag
                v-for="memberId in createGroupForm.memberIds"
                :key="memberId"
                closable
                @close="removeGroupMember(memberId)"
              >
                {{ getFriendName(memberId) }}
              </el-tag>
            </div>
            <div class="friend-list-select">
              <div
                v-for="friend in friends"
                :key="friend.id"
                class="friend-select-item"
                :class="{ selected: createGroupForm.memberIds.includes(friend.id) }"
                @click="toggleGroupMember(friend.id)"
              >
                <el-avatar :size="32" :src="friend.avatar" shape="square">
                  {{ getAvatarFallback(friend.nickname) }}
                </el-avatar>
                <span class="friend-select-name">{{ friend.remark || friend.nickname }}</span>
                <el-icon v-if="createGroupForm.memberIds.includes(friend.id)" class="check-icon"><Check /></el-icon>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createGroupVisible = false">取消</el-button>
        <el-button type="primary" :loading="createGroupLoading" @click="handleCreateGroup">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Check, Warning } from '@element-plus/icons-vue'
import { useChatStore } from '@/store/chat'
import { useUserStore } from '@/store/user'
import { getOrCreatePrivateConversation, getConversationList, createGroupConversation } from '@/api/conversation'
import { getFriendList, getReceivedFriendRequests, deleteFriend, sendFriendRequest as sendFriendRequestApi, handleFriendRequest, updateFriendRemark, blackFriend, unblackFriend, getBlacklist, updateFriendGroup, getFriendGroups } from '@/api/friend'
import { searchUser as searchUserApi } from '@/api/user'

const router = useRouter()
const chatStore = useChatStore()
const userStore = useUserStore()

const friendIdSet = computed(() => new Set(friends.value.map(f => f.id)))
const filteredSearchResults = computed(() => {
  const myId = userStore.userInfo?.id
  return searchResults.value.filter(u => u.id !== myId)
})

const searchKeyword = ref('')
const activeTab = ref('friends')
const tabs = [
  { key: 'friends', label: '好友' },
  { key: 'groups', label: '群聊' },
  { key: 'requests', label: '好友请求' },
  { key: 'blacklist', label: '黑名单' }
]
const selectedContact = ref(null)
const addFriendVisible = ref(false)
const addFriendForm = ref({ keyword: '' })
const searchResults = ref([])
const searchLoading = ref(false)
const searched = ref(false)
let searchTimer = null
const friends = ref([])
const groups = ref([])
const friendRequests = ref([])
const blacklist = ref([])
const friendGroups = ref([])

// 创建群聊相关状态
const createGroupVisible = ref(false)
const createGroupForm = ref({ name: '', memberIds: [] })
const createGroupLoading = ref(false)

// 编辑备注相关状态
const editRemarkVisible = ref(false)
const editRemarkForm = ref({ remark: '' })

const filteredFriends = computed(() => {
  if (!searchKeyword.value) return friends.value
  return friends.value.filter(item =>
    item.nickname.includes(searchKeyword.value) ||
    (item.remark && item.remark.includes(searchKeyword.value))
  )
})

const filteredGroups = computed(() => {
  if (!searchKeyword.value) return groups.value
  return groups.value.filter(item => item.name.includes(searchKeyword.value))
})

const selectContact = (contact) => { selectedContact.value = contact }
const getGenderText = (gender) => {
  if (gender === null || gender === undefined) return '未设置'
  return { 1: '男', 2: '女' }[gender] || '未设置'
}

// 获取头像 fallback 文字（显示最后两个字）
const getAvatarFallback = (name) => {
  if (!name) return '用户'
  return name.length >= 2 ? name.slice(-2) : name
}

const startChat = async () => {
  if (!selectedContact.value) return
  try {
    const res = await getOrCreatePrivateConversation(selectedContact.value.id)
    if (res.code === 200) {
      const conversation = res.data
      const exists = chatStore.conversations.find(c => c.id === conversation.id)
      if (!exists) chatStore.conversations.unshift(conversation)
      await chatStore.selectConversation(conversation)
      router.push('/chat')
    }
  } catch (error) {
    ElMessage.error('创建会话失败')
  }
}

const editRemark = () => {
  if (!selectedContact.value) return
  editRemarkForm.value.remark = selectedContact.value.remark || ''
  editRemarkVisible.value = true
}

const saveRemark = async () => {
  if (!selectedContact.value) return
  try {
    const res = await updateFriendRemark(selectedContact.value.id, editRemarkForm.value.remark)
    if (res.code === 200) {
      // 更新本地数据
      selectedContact.value.remark = editRemarkForm.value.remark
      // 更新好友列表中的备注
      const friend = friends.value.find(f => f.id === selectedContact.value.id)
      if (friend) friend.remark = editRemarkForm.value.remark
      ElMessage.success('备注修改成功')
      editRemarkVisible.value = false
    }
  } catch (error) {
    ElMessage.error('修改备注失败')
  }
}

const deleteFriendAction = () => {
  if (!selectedContact.value) return
  ElMessageBox.confirm('确定要删除该好友吗？', '系统提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteFriend(selectedContact.value.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        selectedContact.value = null
        loadFriends()
      }
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const showAddFriend = () => {
  addFriendVisible.value = true
  addFriendForm.value.keyword = ''
  searchResults.value = []
  searched.value = false
}

const searchUser = async (keyword) => {
  if (!keyword) { searchResults.value = []; searched.value = false; return }
  searchLoading.value = true
  searched.value = false
  try {
    const res = await searchUserApi(keyword)
    if (res.code === 200) { searchResults.value = res.data || []; searched.value = true }
  } catch (error) {
    searchResults.value = []; searched.value = true
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    searchLoading.value = false
  }
}

watch(() => addFriendForm.value.keyword, (newVal) => {
  if (searchTimer) clearTimeout(searchTimer)
  if (!newVal) { searchResults.value = []; searched.value = false; searchLoading.value = false; return }
  searchTimer = setTimeout(() => { searchUser(newVal) }, 300)
})

const sendFriendRequest = async (user) => {
  try {
    const res = await sendFriendRequestApi(user.id, '请求添加好友')
    if (res.code === 200) { ElMessage.success('好友请求已发送'); addFriendVisible.value = false }
  } catch (error) {
    // 处理"已发送过好友请求"的情况
    if (error.code === 3004) {
      ElMessage.warning('已发送过好友请求，请等待对方处理')
    } else {
      console.error('发送好友请求失败:', error)
      ElMessage.error(error.message || '发送好友请求失败')
    }
  }
}

const handleRequest = async (request, status) => {
  try {
    const res = await handleFriendRequest(request.id, status)
    if (res.code === 200) {
      request.status = status
      ElMessage.success(status === 1 ? '已同意' : '已拒绝')
      if (status === 1) loadFriends()
    }
  } catch (error) {
    console.error('处理好友请求失败:', error)
  }
}

const loadFriends = async () => {
  try {
    const res = await getFriendList()
    if (res.code === 200) friends.value = res.data || []
  } catch (error) {
    console.error('加载好友列表失败:', error)
  }
}

const loadFriendRequests = async () => {
  try {
    const res = await getReceivedFriendRequests()
    if (res.code === 200) friendRequests.value = res.data || []
  } catch (error) {
    console.error('加载好友请求失败:', error)
  }
}

const loadGroups = async () => {
  try {
    const res = await getConversationList()
    if (res.code === 200) {
      groups.value = (res.data || []).filter(c => c.conversationType === 2)
    }
  } catch (error) {
    console.error('加载群聊列表失败:', error)
  }
}

const loadBlacklist = async () => {
  try {
    const res = await getBlacklist()
    if (res.code === 200) {
      blacklist.value = res.data || []
    }
  } catch (error) {
    console.error('加载黑名单失败:', error)
  }
}

const loadFriendGroups = async () => {
  try {
    const res = await getFriendGroups()
    if (res.code === 200) {
      friendGroups.value = res.data || []
    }
  } catch (error) {
    console.error('加载分组失败:', error)
  }
}

const handleGroupChange = async (groupName) => {
  if (!selectedContact.value) return
  try {
    await updateFriendGroup(selectedContact.value.id, groupName)
    // 更新本地数据
    selectedContact.value.groupName = groupName
    const friend = friends.value.find(f => f.id === selectedContact.value.id)
    if (friend) friend.groupName = groupName
    ElMessage.success('分组修改成功')
  } catch (error) {
    ElMessage.error('修改分组失败')
  }
}

const handleBlackFriend = async () => {
  if (!selectedContact.value) return
  try {
    await ElMessageBox.confirm(`确定要拉黑 ${selectedContact.value.nickname || selectedContact.value.username} 吗？`, '拉黑好友', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await blackFriend(selectedContact.value.id)
    ElMessage.success('已拉黑')
    selectedContact.value = null
    loadFriends()
    loadBlacklist()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleUnblackFriend = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要取消拉黑 ${user.nickname || user.username} 吗？`, '取消拉黑', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await unblackFriend(user.id)
    ElMessage.success('已取消拉黑')
    loadBlacklist()
    loadFriends()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const showCreateGroup = () => {
  createGroupVisible.value = true
  createGroupForm.value = { name: '', memberIds: [] }
}

const handleCreateGroup = async () => {
  if (!createGroupForm.value.name.trim()) {
    ElMessage.warning('请输入群聊名称')
    return
  }
  if (createGroupForm.value.memberIds.length === 0) {
    ElMessage.warning('请选择至少一位好友')
    return
  }
  createGroupLoading.value = true
  try {
    const res = await createGroupConversation({
      name: createGroupForm.value.name.trim(),
      memberIds: createGroupForm.value.memberIds
    })
    if (res.code === 200) {
      ElMessage.success('群聊创建成功')
      createGroupVisible.value = false
      // 重新加载会话列表（确保包含新创建的群聊）
      await chatStore.loadConversations()
      // 找到新创建的会话并选中
      const conversation = chatStore.conversations.find(c => c.id === res.data.id)
      if (conversation) {
        await chatStore.selectConversation(conversation)
      }
      router.push('/chat')
    }
  } catch (error) {
    ElMessage.error('创建群聊失败')
  } finally {
    createGroupLoading.value = false
  }
}

const startGroupChat = async (group) => {
  try {
    const exists = chatStore.conversations.find(c => c.id === group.id)
    if (!exists) chatStore.conversations.unshift(group)
    await chatStore.selectConversation(group)
    router.push('/chat')
  } catch (error) {
    ElMessage.error('打开群聊失败')
  }
}

const toggleGroupMember = (friendId) => {
  const index = createGroupForm.value.memberIds.indexOf(friendId)
  if (index === -1) {
    createGroupForm.value.memberIds.push(friendId)
  } else {
    createGroupForm.value.memberIds.splice(index, 1)
  }
}

const removeGroupMember = (friendId) => {
  const index = createGroupForm.value.memberIds.indexOf(friendId)
  if (index !== -1) {
    createGroupForm.value.memberIds.splice(index, 1)
  }
}

const getFriendName = (friendId) => {
  const friend = friends.value.find(f => f.id === friendId)
  return friend ? (friend.remark || friend.nickname) : '未知用户'
}

onMounted(() => {
  loadFriends()
  loadFriendRequests()
  loadGroups()
  loadBlacklist()
  loadFriendGroups()
  chatStore.clearUnreadFriendRequests()
})
</script>

<style lang="scss" scoped>
.contacts-container {
  display: flex;
  height: 100%;
  background: #fff;
}

.contacts-sidebar {
  width: 300px;
  border-right: 1px solid #e5e5e5;
  display: flex;
  flex-direction: column;
  background: #fafafa;
}

.contacts-header {
  padding: 12px;
  display: flex;
  gap: 8px;
  border-bottom: 1px solid #e5e5e5;

  :deep(.el-input__wrapper) {
    box-shadow: 0 0 0 1px #e0e0e0 inset;
    border-radius: 0;
    background: #fff;

    &.is-focus {
      box-shadow: 0 0 0 1px #2b7fff inset;
    }
  }

  :deep(.el-button) {
    border-radius: 0;
  }
}

.contacts-tabs {
  display: flex;
  border-bottom: 1px solid #e5e5e5;
}

.tab-item {
  flex: 1;
  padding: 10px;
  text-align: center;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    color: #2b7fff;
  }

  &.active {
    color: #2b7fff;
    border-bottom: 2px solid #2b7fff;
  }
}

.contacts-list {
  flex: 1;
  overflow-y: auto;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.1s;
  border-bottom: 1px solid #f5f5f5;

  &:hover {
    background: #f0f0f0;
  }

  &.active {
    background: #e8f0fe;
  }
}

.contact-info {
  flex: 1;
  margin-left: 10px;
  overflow: hidden;
}

.contact-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 2px;
}

.contact-signature {
  font-size: 12px;
  color: #999;
}

.request-item {
  justify-content: flex-start;
}

.request-actions {
  margin-left: auto;
  display: flex;
  gap: 6px;
}

.status-text {
  font-size: 12px;

  &.agreed { color: #00b42a; }
  &.rejected { color: #f53f3f; }
}

.contact-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  background: #f5f5f5;
}

.detail-header {
  text-align: center;
  margin-bottom: 32px;
}

.detail-name {
  font-size: 20px;
  color: #1a1a1a;
  margin: 12px 0 6px;
  font-weight: 600;
}

.detail-signature {
  font-size: 13px;
  color: #999;
}

.detail-info {
  width: 100%;
  max-width: 400px;
  margin-bottom: 32px;
}

.info-item {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.info-label {
  width: 80px;
  font-size: 13px;
  color: #999;
}

.info-value {
  flex: 1;
  font-size: 13px;
  color: #333;
}

.detail-actions {
  display: flex;
  gap: 12px;

  :deep(.el-button) {
    border-radius: 0;
  }
}

.empty-detail {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;

  p {
    color: #ccc;
    font-size: 14px;
  }
}

.search-results {
  margin-top: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.result-info {
  flex: 1;
  margin-left: 10px;
}

.result-name {
  font-size: 14px;
  color: #333;
}

.result-username {
  font-size: 12px;
  color: #999;
}

.group-member-select {
  width: 100%;
}

.selected-members {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.friend-list-select {
  max-height: 200px;
  overflow-y: auto;
}

.friend-select-item {
  display: flex;
  align-items: center;
  padding: 8px;
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

.friend-select-name {
  flex: 1;
  margin-left: 8px;
  font-size: 14px;
  color: #333;
}

.check-icon {
  color: #2b7fff;
}
</style>

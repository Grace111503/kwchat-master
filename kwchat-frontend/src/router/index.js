import { createRouter, createWebHistory, createWebHashHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { isElectron } from '@/utils/platform'

// 根据环境选择路由模式
// Electron 桌面端：使用 Hash 模式（file:// 协议下 History 模式会导致 404）
// 浏览器 / APK：使用 History 模式（URL 更美观）
const history = isElectron() ? createWebHashHistory() : createWebHistory()

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/debug',
    name: 'Debug',
    component: () => import('@/views/debug/index.vue'),
    meta: { title: '网络调试', requiresAuth: false }
  },
  // ========== 原移动端入口（保持不变，APK 与旧访问路径零影响） ==========
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/chat',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/index.vue'),
        meta: { title: '聊天' }
      },
      {
        path: 'contacts',
        name: 'Contacts',
        component: () => import('@/views/contacts/index.vue'),
        meta: { title: '通讯录' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: () => import('@/views/favorites/index.vue'),
        meta: { title: '我的收藏' }
      }
    ]
  },
  // ========== Web 端并行新入口（平台分发：Capacitor 环境回退到移动布局，Web 环境加载 WebLayout） ==========
  {
    path: '/web',
    component: () => import('@/layouts/MainLayoutRouter.vue'),
    redirect: '/web/chat',
    meta: { requiresAuth: true, isWebEntry: true },
    children: [
      {
        path: 'chat',
        name: 'WebChat',
        component: () => import('@/views/chat/index.vue'),
        meta: { title: '聊天 - Web' }
      },
      {
        path: 'contacts',
        name: 'WebContacts',
        component: () => import('@/views/contacts/index.vue'),
        meta: { title: '通讯录 - Web' }
      },
      {
        path: 'profile',
        name: 'WebProfile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心 - Web' }
      },
      {
        path: 'favorites',
        name: 'WebFavorites',
        component: () => import('@/views/favorites/index.vue'),
        meta: { title: '我的收藏 - Web' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '404' }
  }
]

const router = createRouter({
  history,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 快伟通` : '快伟通'

  if (to.meta.requiresAuth !== false) {
    // 需要认证的页面
    if (!token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  } else {
    // 不需要认证的页面（登录、注册）
    if (token && (to.name === 'Login' || to.name === 'Register')) {
      // 保守方案：登录后统一跳 /chat（原移动端入口），保证零影响
      // 用户如需访问 Web 三栏布局，手动在地址栏输入 /web 即可
      next({ name: 'Chat' })
      return
    }
  }

  next()
})

export default router
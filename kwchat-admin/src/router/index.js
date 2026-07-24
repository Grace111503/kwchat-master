import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'DataBoard' }
      },
      // 用户管理（页面待创建）
      // {
      //   path: 'user',
      //   name: 'User',
      //   redirect: '/user/list',
      //   meta: { title: '用户管理', icon: 'User' },
      //   children: [
      //     {
      //       path: 'list',
      //       name: 'UserList',
      //       component: () => import('@/views/user/list.vue'),
      //       meta: { title: '用户列表' }
      //     },
      //     {
      //       path: 'online',
      //       name: 'UserOnline',
      //       component: () => import('@/views/user/online.vue'),
      //       meta: { title: '在线用户' }
      //     }
      //   ]
      // },
      // 系统设置（页面待创建）
      // {
      //   path: 'system',
      //   name: 'System',
      //   redirect: '/system/config',
      //   meta: { title: '系统设置', icon: 'Setting' },
      //   children: [
      //     {
      //       path: 'config',
      //       name: 'SystemConfig',
      //       component: () => import('@/views/system/config.vue'),
      //       meta: { title: '系统配置' }
      //     },
      //     {
      //       path: 'notification',
      //       name: 'Notification',
      //       component: () => import('@/views/system/notification.vue'),
      //       meta: { title: '通知设置' }
      //     }
      //   ]
      // },
      {
        path: 'ai',
        name: 'AI',
        redirect: '/ai/model',
        meta: { title: 'AI管理', icon: 'Cpu' },
        children: [
          {
            path: 'model',
            name: 'AiModel',
            component: () => import('@/views/ai/model.vue'),
            meta: { title: '模型配置' }
          },
          // 使用统计（页面待创建）
          // {
          //   path: 'usage',
          //   name: 'AiUsage',
          //   component: () => import('@/views/ai/usage.vue'),
          //   meta: { title: '使用统计' }
          // }
        ]
      },
      // 日志管理（页面待创建）
      // {
      //   path: 'log',
      //   name: 'Log',
      //   redirect: '/log/operation',
      //   meta: { title: '日志管理', icon: 'Document' },
      //   children: [
      //     {
      //       path: 'operation',
      //       name: 'OperationLog',
      //       component: () => import('@/views/log/operation.vue'),
      //       meta: { title: '操作日志' }
      //     },
      //     {
      //       path: 'login',
      //       name: 'LoginLog',
      //       component: () => import('@/views/log/login.vue'),
      //       meta: { title: '登录日志' }
      //     }
      //   ]
      // }
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
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 快伟通管理后台` : '快伟通管理后台'

  if (to.meta.requiresAuth !== false) {
    if (!token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
  } else {
    if (token && to.name === 'Login') {
      next({ name: 'Dashboard' })
      return
    }
  }

  next()
})

export default router
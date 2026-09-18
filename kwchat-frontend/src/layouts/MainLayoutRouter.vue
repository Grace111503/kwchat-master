<template>
  <!--
    布局分发器：根据运行平台自动选择合适的布局组件
    - Capacitor 环境（APK）：渲染 MainLayoutMobile（原有移动端布局）
    - Web 环境（浏览器）：渲染 WebLayout（新增三栏布局）
    
    注意：此组件不包含任何业务逻辑，仅做平台路由分发
    回退方案：若分发异常，可直接修改为渲染 MainLayoutMobile
  -->
  <MainLayoutMobile v-if="isCapacitorEnv" />
  <WebLayout v-else />
</template>

<script setup>
import { computed, defineAsyncComponent } from 'vue'
import { isCapacitor } from '@/utils/platform'

// 异步加载两个布局组件，按需加载，不互相影响
const MainLayoutMobile = defineAsyncComponent(() => import('./MainLayoutMobile.vue'))
const WebLayout = defineAsyncComponent(() => import('./WebLayout.vue'))

// 缓存平台检测结果
const isCapacitorEnv = computed(() => isCapacitor())
</script>
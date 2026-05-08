<template>
  <router-view v-if="isLoginPage" />
  <el-container v-else class="shell">
    <el-aside width="232px" class="sidebar">
      <div class="brand">ObservAI</div>
      <el-menu router :default-active="$route.path" class="nav">
        <el-menu-item index="/dashboard"><el-icon><Monitor /></el-icon><span>服务状态</span></el-menu-item>
        <el-menu-item index="/alerts"><el-icon><Warning /></el-icon><span>告警管理</span></el-menu-item>
        <el-menu-item index="/config"><el-icon><Setting /></el-icon><span>告警配置</span></el-menu-item>
        <el-menu-item index="/notifications"><el-icon><Message /></el-icon><span>通知记录</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="topbar">
        <div>
          <strong>AI 运维助手</strong>
          <span>微服务监控与诊断工作台</span>
        </div>
        <el-button :icon="SwitchButton" @click="logout">退出</el-button>
      </el-header>
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Monitor, Warning, Setting, Message, SwitchButton } from '@element-plus/icons-vue'
import { clearToken } from './stores/auth'

const route = useRoute()
const router = useRouter()
const isLoginPage = computed(() => route.path === '/login')

function logout() {
  clearToken()
  router.push('/login')
}
</script>


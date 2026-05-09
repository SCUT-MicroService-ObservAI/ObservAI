<template>
  <div class="login-page">
    <div class="login-panel">
      <h1>ObservAI</h1>
      <p>AI 运维助手</p>
      <el-form :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item label="账号">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" class="login-button" @click="submit">登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/observai'
import { setToken } from '../stores/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: 'ops', password: '123456' })

async function submit() {
  loading.value = true
  try {
    const res = await login(form)
    setToken(res.data.token)
    router.push('/dashboard')
  } catch (error) {
    ElMessage.error('登录失败，请检查账号或密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-button {
  width: 100%;
}
</style>


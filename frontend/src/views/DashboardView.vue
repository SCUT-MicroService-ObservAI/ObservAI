<template>
  <div class="page-title">
    <h1>服务状态</h1>
    <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
  </div>

  <div class="stat-grid">
    <div class="panel"><el-statistic title="总服务数" :value="services.length" /></div>
    <div class="panel"><el-statistic title="异常服务数" :value="abnormalCount" /></div>
    <div class="panel"><el-statistic title="最近采集" :value="latestTime" /></div>
  </div>

  <div class="service-grid">
    <div v-for="service in services" :key="service.serviceName" class="panel">
      <div class="page-title">
        <h1>{{ service.serviceName }}</h1>
        <el-tag :type="service.status === 'UP' ? 'success' : service.status === 'ABNORMAL' ? 'warning' : 'danger'">
          {{ service.status }}
        </el-tag>
      </div>
      <div class="metric-line"><span>CPU</span><el-progress :percentage="format(service.cpu)" /></div>
      <div class="metric-line"><span>内存</span><el-progress :percentage="format(service.memory)" /></div>
      <div class="metric-line"><span>错误率</span><strong>{{ format(service.errorRate) }}%</strong></div>
      <div class="metric-line"><span>响应时间</span><strong>{{ format(service.responseTime) }} ms</strong></div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { fetchServices } from '../api/observai'

const loading = ref(false)
const services = ref([])
let timer

const abnormalCount = computed(() => services.value.filter((item) => item.status !== 'UP').length)
const latestTime = computed(() => services.value[0]?.timestamp?.replace('T', ' ') ?? '-')

function format(value) {
  return Number(value ?? 0).toFixed(1)
}

async function load() {
  loading.value = true
  try {
    const res = await fetchServices()
    services.value = res.data ?? []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
  timer = window.setInterval(load, 15000)
})

onUnmounted(() => window.clearInterval(timer))
</script>


<template>
  <div class="page-title">
    <h1>告警管理</h1>
  </div>
  <div class="toolbar">
    <el-select v-model="filters.status" clearable placeholder="状态" style="width: 160px">
      <el-option v-for="item in statuses" :key="item" :label="item" :value="item" />
    </el-select>
    <el-select v-model="filters.severity" clearable placeholder="严重等级" style="width: 160px">
      <el-option v-for="item in severities" :key="item" :label="item" :value="item" />
    </el-select>
    <el-input v-model="filters.serviceName" clearable placeholder="服务名称" style="width: 220px" />
    <el-button :icon="Search" type="primary" @click="load">查询</el-button>
  </div>
  <el-table :data="alerts" class="panel" row-key="alertId">
    <el-table-column prop="alertId" label="ID" width="90" />
    <el-table-column prop="serviceName" label="服务" min-width="180" />
    <el-table-column prop="alertType" label="类型" min-width="170" />
    <el-table-column prop="severity" label="等级" width="110" />
    <el-table-column prop="status" label="状态" width="130" />
    <el-table-column prop="diagnosisStatus" label="诊断" width="120" />
    <el-table-column prop="triggerCount" label="触发" width="90" />
    <el-table-column label="操作" width="110">
      <template #default="{ row }">
        <el-button link type="primary" @click="$router.push(`/alerts/${row.alertId}`)">详情</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { fetchAlerts } from '../api/observai'

const statuses = ['UNHANDLED', 'PROCESSING', 'RESOLVED', 'IGNORED', 'FALSE_ALARM', 'RECOVERED']
const severities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
const filters = reactive({ status: '', severity: '', serviceName: '' })
const alerts = ref([])

async function load() {
  const params = Object.fromEntries(Object.entries(filters).filter(([, value]) => value))
  const res = await fetchAlerts(params)
  alerts.value = res.data ?? []
}

onMounted(load)
</script>


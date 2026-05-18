<template>
  <div class="page-title">
    <h1>通知记录</h1>
    <el-button :icon="Refresh" @click="load">刷新</el-button>
  </div>

  <el-table :data="records" class="panel">
    <el-table-column prop="alertId" label="告警 ID" width="100" />
    <el-table-column prop="email" label="邮箱" min-width="180">
      <template #default="{ row }">{{ row.email || '-' }}</template>
    </el-table-column>
    <el-table-column prop="title" label="标题" min-width="240" />
    <el-table-column prop="status" label="状态" width="120">
      <template #default="{ row }">
        <el-tag :type="statusTag(row.status)">{{ row.status }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="errorMessage" label="失败/跳过原因" min-width="220">
      <template #default="{ row }">{{ row.errorMessage || '-' }}</template>
    </el-table-column>
    <el-table-column prop="sentAt" label="发送时间" min-width="180">
      <template #default="{ row }">{{ row.sentAt || '-' }}</template>
    </el-table-column>
    <el-table-column prop="createdAt" label="创建时间" min-width="180" />
  </el-table>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { fetchNotificationRecords } from '../api/observai'

const records = ref([])

async function load() {
  const res = await fetchNotificationRecords()
  records.value = res.data ?? []
}

function statusTag(status) {
  return {
    PENDING: 'warning',
    SUCCESS: 'success',
    FAILED: 'danger',
    SKIPPED: 'info'
  }[status] || ''
}

onMounted(load)
</script>

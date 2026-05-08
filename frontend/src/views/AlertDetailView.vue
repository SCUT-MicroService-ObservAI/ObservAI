<template>
  <div class="page-title">
    <h1>告警详情 #{{ id }}</h1>
    <el-button @click="$router.push('/alerts')">返回</el-button>
  </div>

  <el-row :gutter="16" v-if="detail">
    <el-col :xs="24" :lg="12">
      <div class="panel">
        <h2>基础信息</h2>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="服务">{{ detail.alert.serviceName }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.alert.alertType }}</el-descriptions-item>
          <el-descriptions-item label="等级">{{ detail.alert.severity }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.alert.status }}</el-descriptions-item>
          <el-descriptions-item label="诊断">{{ detail.alert.diagnosisStatus }}</el-descriptions-item>
          <el-descriptions-item label="触发次数">{{ detail.alert.triggerCount }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-col>
    <el-col :xs="24" :lg="12">
      <div class="panel">
        <h2>处理状态</h2>
        <div class="toolbar">
          <el-select v-model="statusForm.status" style="width: 180px">
            <el-option v-for="item in statuses" :key="item" :label="item" :value="item" />
          </el-select>
          <el-input v-model="statusForm.remark" placeholder="备注" />
          <el-button type="primary" @click="saveStatus">保存</el-button>
        </div>
        <el-timeline>
          <el-timeline-item v-for="item in detail.statusHistory" :key="item.id" :timestamp="item.createdAt">
            {{ item.fromStatus || '-' }} → {{ item.toStatus }}，{{ item.remark || '无备注' }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-col>
  </el-row>

  <div class="panel detail-section" v-if="detail">
    <h2>指标快照</h2>
    <pre>{{ detail.alert.metricsSnapshot }}</pre>
    <h2>异常日志</h2>
    <pre>{{ detail.alert.logSnippet || '-' }}</pre>
    <h2>AI 诊断</h2>
    <pre>{{ detail.alert.diagnosisResult || '诊断任务尚未完成' }}</pre>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAlertDetail, updateAlertStatus } from '../api/observai'

const props = defineProps({ id: String })
const statuses = ['UNHANDLED', 'PROCESSING', 'RESOLVED', 'IGNORED', 'FALSE_ALARM', 'RECOVERED']
const detail = ref(null)
const statusForm = reactive({ status: 'PROCESSING', remark: '' })

async function load() {
  const res = await fetchAlertDetail(props.id)
  detail.value = res.data
  statusForm.status = detail.value.alert.status
}

async function saveStatus() {
  await updateAlertStatus(props.id, statusForm)
  ElMessage.success('状态已更新')
  load()
}

onMounted(load)
</script>

<style scoped>
.detail-section {
  margin-top: 16px;
}

pre {
  white-space: pre-wrap;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 12px;
}
</style>


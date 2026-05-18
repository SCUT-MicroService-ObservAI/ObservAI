<template>
  <div class="page-title">
    <h1>告警详情 #{{ id }}</h1>
    <div class="page-actions">
      <el-button @click="load">刷新</el-button>
      <el-button @click="$router.push('/alerts')">返回</el-button>
    </div>
  </div>

  <el-row :gutter="16" v-if="detail">
    <el-col :xs="24" :lg="12">
      <div class="panel">
        <h2>基础信息</h2>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="服务">{{ detail.alert.serviceName }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.alert.alertType }}</el-descriptions-item>
          <el-descriptions-item label="指标">{{ detail.alert.metricName }}</el-descriptions-item>
          <el-descriptions-item label="等级">
            <el-tag :type="severityTag(detail.alert.severity)">{{ detail.alert.severity }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(detail.alert.status)">{{ detail.alert.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="诊断状态">{{ detail.alert.diagnosisStatus }}</el-descriptions-item>
          <el-descriptions-item label="诊断来源">
            <el-tag v-if="diagnosisSource === 'ALIYUN_AI'" type="success">阿里云 AI</el-tag>
            <el-tag v-else-if="diagnosisSource === 'MOCK'" type="info">Mock 兜底</el-tag>
            <span v-else>{{ diagnosisSource || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="触发次数">{{ detail.alert.triggerCount }}</el-descriptions-item>
          <el-descriptions-item label="首次触发">{{ detail.alert.firstTriggeredAt }}</el-descriptions-item>
          <el-descriptions-item label="最近触发">{{ detail.alert.lastTriggeredAt }}</el-descriptions-item>
          <el-descriptions-item label="最近通知">{{ detail.alert.lastNotifiedAt || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-col>

    <el-col :xs="24" :lg="12">
      <div class="panel">
        <h2>处理状态</h2>
        <div class="toolbar status-toolbar">
          <el-select v-model="statusForm.status" class="status-select">
            <el-option v-for="item in statuses" :key="item" :label="item" :value="item" />
          </el-select>
          <el-input v-model="statusForm.remark" placeholder="备注" />
          <el-button type="primary" @click="saveStatus">保存</el-button>
          <el-button type="success" plain @click="markRecovered">系统恢复</el-button>
        </div>
        <el-timeline>
          <el-timeline-item v-for="item in detail.statusHistory" :key="item.id" :timestamp="item.createdAt">
            <strong>{{ item.fromStatus || '-' }} -> {{ item.toStatus }}</strong>
            <span class="history-meta"> {{ item.operator || 'system' }}</span>
            <div class="history-remark">{{ item.remark || '无备注' }}</div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-col>
  </el-row>

  <div class="panel detail-section" v-if="detail">
    <h2>指标快照</h2>
    <pre>{{ metricsText }}</pre>
    <h2>异常日志</h2>
    <pre>{{ detail.alert.logSnippet || '-' }}</pre>
    <h2>AI 诊断</h2>
    <div class="diagnosis-toolbar">
      <el-button type="primary" plain :loading="rediagnoseLoading" @click="onRediagnose">
        重新诊断
      </el-button>
      <span class="hint">诊断失败、超时或格式异常时会保存 Mock 兜底结果。</span>
    </div>
    <pre>{{ diagnosisResultText }}</pre>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAlertDetail, recoverAlert, rediagnoseAlert, updateAlertStatus } from '../api/observai'

const props = defineProps({ id: String })
const statuses = ['UNHANDLED', 'PROCESSING', 'RESOLVED', 'IGNORED', 'FALSE_ALARM', 'RECOVERED']
const detail = ref(null)
const statusForm = reactive({ status: 'PROCESSING', remark: '', operator: 'user' })
const rediagnoseLoading = ref(false)

const diagnosisSource = computed(() => {
  const r = detail.value?.alert?.diagnosisResult
  if (!r) return ''
  if (typeof r === 'object' && r.source) return r.source
  if (typeof r === 'string') {
    try {
      return JSON.parse(r).source || ''
    } catch {
      return ''
    }
  }
  return ''
})

const diagnosisResultText = computed(() => {
  const r = detail.value?.alert?.diagnosisResult
  if (r == null) return '诊断任务尚未完成'
  if (typeof r === 'string') return r
  return JSON.stringify(r, null, 2)
})

const metricsText = computed(() => {
  const metrics = detail.value?.alert?.metricsSnapshot
  if (!metrics) return '-'
  if (typeof metrics === 'string') return metrics
  return JSON.stringify(metrics, null, 2)
})

async function load() {
  const res = await fetchAlertDetail(props.id)
  detail.value = res.data
  statusForm.status = detail.value.alert.status
  statusForm.remark = ''
}

async function saveStatus() {
  await updateAlertStatus(props.id, statusForm)
  ElMessage.success('状态已更新')
  await load()
}

async function markRecovered() {
  await recoverAlert(props.id)
  ElMessage.success('已标记为恢复')
  await load()
}

async function onRediagnose() {
  rediagnoseLoading.value = true
  try {
    await rediagnoseAlert(props.id)
    await load()
    ElMessage.success('重新诊断已完成')
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '请求失败'
    ElMessage.error(msg)
  } finally {
    rediagnoseLoading.value = false
  }
}

function severityTag(severity) {
  return {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'danger',
    CRITICAL: 'danger'
  }[severity] || ''
}

function statusTag(status) {
  return {
    UNHANDLED: 'danger',
    PROCESSING: 'warning',
    RESOLVED: 'success',
    IGNORED: 'info',
    FALSE_ALARM: 'info',
    RECOVERED: 'success'
  }[status] || ''
}

onMounted(load)
</script>

<style scoped>
.page-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.page-actions,
.status-toolbar,
.diagnosis-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.status-select {
  width: 180px;
}

.history-meta {
  color: #64748b;
  font-size: 13px;
}

.history-remark {
  margin-top: 4px;
  color: #334155;
}

.diagnosis-toolbar {
  margin-bottom: 8px;
}

.diagnosis-toolbar .hint {
  font-size: 13px;
  color: #64748b;
}

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

<template>
  <div class="page-title">
    <h1>告警配置</h1>
  </div>
  <el-tabs v-model="active">
    <el-tab-pane label="告警规则配置" name="rules">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openRule()">新增规则</el-button>
      </div>
      <el-table :data="rules" class="panel">
        <el-table-column prop="serviceName" label="服务" min-width="180" />
        <el-table-column prop="metricName" label="指标" width="130" />
        <el-table-column prop="operator" label="比较符" width="90" />
        <el-table-column prop="threshold" label="阈值" width="100" />
        <el-table-column prop="severity" label="等级" width="110" />
        <el-table-column label="启用" width="90">
          <template #default="{ row }"><el-switch v-model="row.enabled" @change="toggleAlertRule(row.id, row.enabled)" /></template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRule(row)">编辑</el-button>
            <el-button link type="danger" @click="removeRule(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>
    <el-tab-pane label="邮件通知配置" name="mail">
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="openMail()">新增邮箱</el-button>
      </div>
      <el-table :data="configs" class="panel">
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column prop="minSeverity" label="最低等级" width="130" />
        <el-table-column label="启用" width="90">
          <template #default="{ row }"><el-switch v-model="row.enabled" @change="toggleNotificationConfig(row.id, row.enabled)" /></template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="openMail(row)">编辑</el-button>
            <el-button link type="danger" @click="removeMail(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-tab-pane>
  </el-tabs>

  <el-dialog v-model="ruleDialog" title="告警规则" width="520px">
    <el-form :model="ruleForm" label-width="110px">
      <el-form-item label="服务"><el-input v-model="ruleForm.serviceName" /></el-form-item>
      <el-form-item label="指标"><el-input v-model="ruleForm.metricName" /></el-form-item>
      <el-form-item label="比较符"><el-select v-model="ruleForm.operator"><el-option v-for="item in operators" :key="item" :label="item" :value="item" /></el-select></el-form-item>
      <el-form-item label="阈值"><el-input-number v-model="ruleForm.threshold" /></el-form-item>
      <el-form-item label="持续秒数"><el-input-number v-model="ruleForm.durationSeconds" /></el-form-item>
      <el-form-item label="等级"><el-select v-model="ruleForm.severity"><el-option v-for="item in severities" :key="item" :label="item" :value="item" /></el-select></el-form-item>
      <el-form-item label="启用"><el-switch v-model="ruleForm.enabled" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="ruleDialog = false">取消</el-button><el-button type="primary" @click="saveRule">保存</el-button></template>
  </el-dialog>

  <el-dialog v-model="mailDialog" title="邮件通知" width="520px">
    <el-form :model="mailForm" label-width="110px">
      <el-form-item label="邮箱"><el-input v-model="mailForm.email" /></el-form-item>
      <el-form-item label="最低等级"><el-select v-model="mailForm.minSeverity"><el-option v-for="item in severities" :key="item" :label="item" :value="item" /></el-select></el-form-item>
      <el-form-item label="启用"><el-switch v-model="mailForm.enabled" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="mailDialog = false">取消</el-button><el-button type="primary" @click="saveMail">保存</el-button></template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import {
  createAlertRule, createNotificationConfig, deleteAlertRule, deleteNotificationConfig,
  fetchAlertRules, fetchNotificationConfigs, toggleAlertRule, toggleNotificationConfig,
  updateAlertRule, updateNotificationConfig
} from '../api/observai'

const active = ref('rules')
const rules = ref([])
const configs = ref([])
const severities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
const operators = ['>', '>=', '<', '<=', '==']
const ruleDialog = ref(false)
const mailDialog = ref(false)
const ruleForm = reactive({})
const mailForm = reactive({})

async function load() {
  rules.value = (await fetchAlertRules()).data ?? []
  configs.value = (await fetchNotificationConfigs()).data ?? []
}

function openRule(row) {
  Object.keys(ruleForm).forEach((key) => delete ruleForm[key])
  Object.assign(ruleForm, row ?? { serviceName: 'demo-order-service', metricName: 'errorRate', operator: '>', threshold: 10, durationSeconds: 60, severity: 'HIGH', enabled: true })
  ruleDialog.value = true
}

async function saveRule() {
  ruleForm.id ? await updateAlertRule(ruleForm.id, ruleForm) : await createAlertRule(ruleForm)
  ruleDialog.value = false
  load()
}

async function removeRule(id) {
  await deleteAlertRule(id)
  load()
}

function openMail(row) {
  Object.keys(mailForm).forEach((key) => delete mailForm[key])
  Object.assign(mailForm, row ?? { email: 'ops@example.com', minSeverity: 'HIGH', enabled: true })
  mailDialog.value = true
}

async function saveMail() {
  mailForm.id ? await updateNotificationConfig(mailForm.id, mailForm) : await createNotificationConfig(mailForm)
  mailDialog.value = false
  load()
}

async function removeMail(id) {
  await deleteNotificationConfig(id)
  load()
}

onMounted(load)
</script>

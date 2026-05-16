import http from './http'

export const login = (payload) => http.post('/login', payload)
export const fetchServices = () => http.get('/monitor/services')
export const collectNow = () => http.post('/monitor/collect')
export const fetchAlerts = (params) => http.get('/alerts', { params })
export const fetchAlertDetail = (id) => http.get(`/alerts/${id}`)
export const updateAlertStatus = (id, payload) => http.put(`/alerts/${id}/status`, payload)
export const rediagnoseAlert = (id) =>
  http.post(`/alerts/${id}/rediagnose`, null, { timeout: 120000 })
export const fetchAlertRules = () => http.get('/alert-rules')
export const createAlertRule = (payload) => http.post('/alert-rules', payload)
export const updateAlertRule = (id, payload) => http.put(`/alert-rules/${id}`, payload)
export const toggleAlertRule = (id, enabled) => http.put(`/alert-rules/${id}/enabled`, { enabled })
export const deleteAlertRule = (id) => http.delete(`/alert-rules/${id}`)
export const fetchNotificationConfigs = () => http.get('/notification/configs')
export const createNotificationConfig = (payload) => http.post('/notification/configs', payload)
export const updateNotificationConfig = (id, payload) => http.put(`/notification/configs/${id}`, payload)
export const toggleNotificationConfig = (id, enabled) => http.put(`/notification/configs/${id}/enabled`, { enabled })
export const deleteNotificationConfig = (id) => http.delete(`/notification/configs/${id}`)
export const fetchNotificationRecords = (params) => http.get('/notification/records', { params })


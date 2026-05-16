import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import AlertsView from '../views/AlertsView.vue'
import AlertDetailView from '../views/AlertDetailView.vue'
import ConfigView from '../views/ConfigView.vue'
import NotificationsView from '../views/NotificationsView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView, meta: { public: true } },
    { path: '/', redirect: '/dashboard' },
    { path: '/dashboard', component: DashboardView },
    { path: '/alerts', component: AlertsView },
    { path: '/alerts/:id', component: AlertDetailView, props: true },
    { path: '/config', component: ConfigView },
    { path: '/notifications', component: NotificationsView }
  ]
})

router.beforeEach((to) => {
  if (!to.meta.public && !getToken()) {
    return '/login'
  }
})

export default router


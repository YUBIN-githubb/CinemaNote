import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue') },
  { path: '/signup', component: () => import('../views/SignupView.vue') },
  {
    path: '/',
    component: () => import('../views/ArchiveView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/archive/new',
    component: () => import('../views/ArchiveCreateView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/archive/:id',
    component: () => import('../views/ArchiveDetailView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/archive/:id/edit',
    component: () => import('../views/ArchiveEditView.vue'),
    meta: { requiresAuth: true },
  },
  { path: '/share/:token', component: () => import('../views/SharedView.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return '/login'
  }
  if ((to.path === '/login' || to.path === '/signup') && authStore.isLoggedIn) {
    return '/'
  }
})

export default router

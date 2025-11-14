import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'

// 路由守卫，检查是否已登录
const isAuthenticated = () => {
  return !!localStorage.getItem('access_token')
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Home',
      component: Home,
      meta: { requiresAuth: true }
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
    }
  ]
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !isAuthenticated()) {
    // 如果需要认证但未登录，重定向到登录页
    next('/login')
  } else if (to.name === 'Login' && isAuthenticated()) {
    // 如果已登录但访问登录页，重定向到首页
    next('/')
  } else {
    // 其他情况正常导航
    next()
  }
})

export default router
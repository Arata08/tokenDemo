<template>
      <div class="login-header">
        <h1>JWT Token Manager</h1>
        <p>请使用您的账户登录</p>
      </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
            <i class="icon-user"></i>
            <input 
              id="username" 
              v-model="loginForm.username" 
              type="text" 
              placeholder="请输入用户名" 
              required
            />

            <i class="icon-lock"></i>
            <input 
              id="password" 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              required
            />
        
        <button type="submit" :disabled="loading" class="btn-login">
          <span v-if="loading" class="spinner"></span>
          {{ loading ? '登录中...' : '登录' }}
        </button>
        
        <div v-if="errorMessage" class="error-message">
          <i class="icon-error"></i>
          {{ errorMessage }}
        </div>
      </form>
      
      <div class="login-footer">
        <p>默认账户: admin / password</p>
      </div>
</template>

<script>
import apiClient from '../api/index.js'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: 'admin',
        password: 'password'
      },
      loading: false,
      errorMessage: ''
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true
      this.errorMessage = ''
      
      try {
        const response = await apiClient.post('/auth/login', this.loginForm)
        const { access_token, refresh_token } = response.data
        
        // 存储token到localStorage
        localStorage.setItem('access_token', access_token)
        localStorage.setItem('refresh_token', refresh_token)
        
        // 跳转到首页
        this.$router.push('/')
      } catch (error) {
        console.error('Login failed:', error)
        this.errorMessage = '登录失败，请检查用户名和密码'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
</style>
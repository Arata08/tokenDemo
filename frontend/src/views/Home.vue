<template>
  <div class="modern-home">
    <!-- 顶部导航栏 -->
    <header class="top-nav">
      <div class="nav-brand">
        <h1>JWT Token Manager</h1>
      </div>
      <div class="nav-actions">
        <button @click="handleLogout" class="btn btn-outline">
          <i class="icon-logout"></i>
          退出登录
        </button>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 令牌状态卡片 -->
      <section class="token-stats">
        <div class="stats-grid">
          <div class="stat-card card-user">
            <div class="card-icon">
              <i class="icon-user"></i>
            </div>
            <div class="card-info">
              <h3>当前用户</h3>
              <p class="info-value">{{ currentUser || '未登录' }}</p>
            </div>
          </div>

          <div class="stat-card card-access">
            <div class="card-icon">
              <i class="icon-token"></i>
            </div>
            <div class="card-info">
              <h3>Access Token</h3>
              <p class="info-value">{{ accessTokenExpiry || '未获取' }}</p>
            </div>
          </div>

          <div class="stat-card card-refresh">
            <div class="card-icon">
              <i class="icon-refresh"></i>
            </div>
            <div class="card-info">
              <h3>Refresh Token</h3>
              <p class="info-value">{{ refreshTokenExpiry || '未获取' }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- 操作面板 -->
      <section class="action-panel">
        <div class="panel-header">
          <h2>操作面板</h2>
          <p>执行各种与令牌相关的操作</p>
        </div>

        <div class="panel-actions">
          <button @click="fetchProtectedData" :disabled="loading" class="btn btn-primary">
            <span v-if="loading" class="spinner"></span>
            {{ loading ? '请求中...' : '获取受保护数据' }}
          </button>
          <button @click="checkTokenStatus" class="btn btn-secondary">
            <i class="icon-check"></i>
            检查Token状态
          </button>
        </div>
      </section>

      <!-- 响应结果 -->
      <section class="response-section" v-if="apiResponse || error || notification">
        <div class="section-header">
          <h2>响应结果</h2>
          <p>API调用的返回数据</p>
        </div>

        <div class="response-container">
          <div v-if="notification" class="response-card card-success">
            <div class="card-header">
              <h3>
                <i class="icon-success"></i>
                提示信息
              </h3>
            </div>
            <div class="card-body">
              <p>{{ notification }}</p>
            </div>
          </div>

          <div v-if="apiResponse" class="response-card card-success">
            <div class="card-header">
              <h3>
                <i class="icon-success"></i>
                成功响应
              </h3>
            </div>
            <div class="card-body">
              <pre>{{ formattedResponse }}</pre>
            </div>
          </div>

          <div v-if="error" class="response-card card-error">
            <div class="card-header">
              <h3>
                <i class="icon-error"></i>
                错误信息
              </h3>
            </div>
            <div class="card-body">
              <p>{{ error }}</p>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import apiClient from '../api/index.js'

export default {
  name: 'Home',
  data() {
    return {
      currentUser: '',
      accessTokenExpiry: '',
      refreshTokenExpiry: '',
      apiResponse: null,
      error: '',
      loading: false,
      notification: '' // 添加通知字段
    }
  },
  computed: {
    formattedResponse() {
      return JSON.stringify(this.apiResponse, null, 2)
    }
  },
  mounted() {
    this.checkTokenStatus()
  },
  methods: {
    // 解析JWT token获取过期时间
    parseJwt(token) {
      try {
        const base64Url = token.split('.')[1]
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
          return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
        }).join(''))
        
        return JSON.parse(jsonPayload)
      } catch (error) {
        return null
      }
    },
    
    // 检查token状态
    checkTokenStatus() {
      const accessToken = localStorage.getItem('access_token')
      const refreshToken = localStorage.getItem('refresh_token')
      
      if (accessToken) {
        const payload = this.parseJwt(accessToken)
        if (payload) {
          this.currentUser = payload.sub
          this.accessTokenExpiry = new Date(payload.exp * 1000).toLocaleString()
        }
      }
      
      if (refreshToken) {
        const payload = this.parseJwt(refreshToken)
        if (payload) {
          this.refreshTokenExpiry = new Date(payload.exp * 1000).toLocaleString()
        }
      }
      
      // 显示通知
      this.notification = 'Token状态已更新';
      setTimeout(() => {
        this.notification = '';
      }, 3000);
    },
    
    // 获取受保护的数据
    async fetchProtectedData() {
      this.loading = true
      this.apiResponse = null
      this.error = ''
      
      try {
        const response = await apiClient.get('/resource/data')
        this.apiResponse = response.data
      } catch (error) {
        console.error('API call failed:', error)
        this.error = error.response?.data?.error || '请求失败'
      } finally {
        this.loading = false
      }
    },
    
    // 退出登录
    handleLogout() {
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      this.$router.push('/login')
    }
  }
}
</script>

<style scoped>
/* 现代化样式重写 */
.modern-home {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 顶部导航栏 */
.top-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 15px 25px;
  margin-bottom: 30px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.nav-brand h1 {
  color: white;
  font-size: 1.8rem;
  font-weight: 600;
  margin: 0;
}

.nav-actions .btn {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-actions .btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

/* 主要内容区域 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
}

/* 令牌状态卡片 */
.token-stats {
  margin-bottom: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 25px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.card-user .card-icon {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  color: #fff;
}

.card-access .card-icon {
  background: linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%);
  color: #fff;
}

.card-refresh .card-icon {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  color: #fff;
}

.card-info h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1.1rem;
  font-weight: 600;
}

.info-value {
  margin: 0;
  color: #666;
  font-size: 0.95rem;
  font-weight: 500;
}

/* 操作面板 */
.action-panel {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 30px;
  margin-bottom: 30px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.panel-header {
  margin-bottom: 25px;
}

.panel-header h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 1.5rem;
  font-weight: 600;
}

.panel-header p {
  margin: 0;
  color: #666;
  font-size: 1rem;
}

.panel-actions {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.9);
  color: #333;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.btn-secondary:hover {
  background: rgba(245, 245, 245, 0.9);
  transform: translateY(-2px);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应结果 */
.section-header {
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0 0 8px 0;
  color: white;
  font-size: 1.5rem;
  font-weight: 600;
}

.section-header p {
  margin: 0;
  color: rgba(255, 255, 255, 0.8);
  font-size: 1rem;
}

.response-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.response-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.card-header {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-header h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-success .card-header {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.card-error .card-header {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.card-body {
  padding: 20px;
}

.card-body pre {
  margin: 0;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 0.9rem;
  line-height: 1.5;
}

.card-body p {
  margin: 0;
  color: #333;
  font-size: 1rem;
  line-height: 1.5;
}

/* 图标字体 */
.icon-user::before {
  content: "👤";
}

.icon-token::before {
  content: "🔑";
}

.icon-refresh::before {
  content: "🔄";
}

.icon-check::before {
  content: "✅";
}

.icon-success::before {
  content: "✔️";
}

.icon-error::before {
  content: "❌";
}

.icon-logout::before {
  content: "🚪";
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modern-home {
    padding: 15px;
  }

  .top-nav {
    flex-direction: column;
    gap: 15px;
    padding: 15px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: 20px;
  }

  .panel-actions {
    flex-direction: column;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}
</style>

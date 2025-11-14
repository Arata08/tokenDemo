import axios from 'axios'

// 创建 axios 实例
const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端服务地址
  timeout: 10000
})

// 请求拦截器：在每个请求头中添加 access_token
apiClient.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('access_token')
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 刷新token的锁，防止并发刷新
let isRefreshing = false
// 失败请求队列
let failedQueue = []

// 处理请求队列
const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })

  failedQueue = []
}

// 响应拦截器：处理 401 错误并尝试刷新 token
apiClient.interceptors.response.use(
  (response) => {
    // 对响应数据做点什么
    return response
  },
  async (error) => {
    const originalRequest = error.config

    // 如果是401错误且未重试过
    if (error.response?.status === 401 && !originalRequest._retry) {
      // 如果正在刷新token，则将请求加入队列等待
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers['Authorization'] = 'Bearer ' + token
          return apiClient(originalRequest)
        }).catch(err => {
          return Promise.reject(err)
        })
      }

      // 标记为已重试，防止无限循环
      originalRequest._retry = true
      isRefreshing = true

      // 获取refresh_token
      const refreshToken = localStorage.getItem('refresh_token')

      // 如果没有refresh_token，直接reject错误
      if (!refreshToken) {
        isRefreshing = false
        // 清除本地token
        localStorage.removeItem('access_token')
        localStorage.removeItem('refresh_token')
        return Promise.reject(error)
      }

      try {
        // 发起刷新token请求
        const refreshResponse = await axios.post('http://localhost:8080/api/auth/refresh', {}, {
          headers: {
            Authorization: `Bearer ${refreshToken}`
          }
        })

        const newAccessToken = refreshResponse.data.access_token
        // const newRefreshToken = refreshResponse.data.refresh_token // 如果后端也返回了新的 refresh_token

        // 更新本地存储的 token
        localStorage.setItem('access_token', newAccessToken)
        // localStorage.setItem('refresh_token', newRefreshToken) // 如果更新了

        // 更新正在请求的 axios 实例的 header
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${newAccessToken}`

        // 重试原始请求
        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`
        processQueue(null, newAccessToken)
        return apiClient(originalRequest)
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError)
        processQueue(refreshError, null)
        // Refresh token 也失败了，清除本地 Token
        localStorage.removeItem('access_token')
        localStorage.removeItem('refresh_token')
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default apiClient
# token无感刷新全流程java、Vue 、Axios

## 流程总结

1.  用户登录成功，后端返回 `access_token` 和 `refresh_token`，前端存储。
2.  前端每次 API 请求时，通过 Axios 请求拦截器自动在 `Authorization` 头中添加 `Bearer access_token`。
3.  后端通过过滤器验证 `access_token`。
4.  当 `access_token` 过期，后端返回 401。
5.  前端 Axios 响应拦截器捕获 401 错误。
6.  拦截器发起 `/auth/refresh` 请求，携带 `refresh_token`。
7.  后端验证 `refresh_token`，成功则返回新的 `access_token`。
8.  前端更新本地 `access_token`。
9.  拦截器自动重试之前失败的请求（使用新 `access_token`）。
10. 如果 `refresh_token` 也失败或不存在，则清除本地 Token，跳转登录页。
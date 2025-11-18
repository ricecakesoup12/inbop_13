import axios from 'axios'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8081',
  timeout: 10000,
})

// 요청 인터셉터
http.interceptors.request.use(
  (config) => {
    // 토큰이 있다면 헤더에 추가
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 응답 인터셉터
http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const url = error?.config?.url || ''

    // /pending API는 404가 정상 케이스이므로 콘솔에 에러를 남기지 않음
    const isExpectedPending404 =
      status === 404 && url.includes('/api/exercise-prescriptions/user/') && url.endsWith('/pending')

    if (!isExpectedPending404) {
      console.error('API Error:', error)
    }

    return Promise.reject(error)
  }
)

export default http

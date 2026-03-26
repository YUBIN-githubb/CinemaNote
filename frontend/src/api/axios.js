import axios from 'axios'

const instance = axios.create({
  baseURL: '',
  withCredentials: true,
})

instance.interceptors.response.use(
  (response) => {
    return response.data.data
  },
  (error) => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      localStorage.removeItem('user')
      window.location.replace('/login')
      return Promise.reject(error)
    }
    const message =
      error.response?.data?.message || '요청 처리 중 오류가 발생했습니다.'
    return Promise.reject(new Error(message))
  }
)

export default instance

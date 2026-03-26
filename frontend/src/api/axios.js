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
    const message =
      error.response?.data?.message || '요청 처리 중 오류가 발생했습니다.'
    return Promise.reject(new Error(message))
  }
)

export default instance

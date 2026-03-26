import api from './axios.js'

export const createShareLink = () => api.post('/api/share')

export const deleteShareLink = () => api.delete('/api/share')

export const getSharedArchives = (token, page = 0) =>
  api.get(`/api/share/${token}`, { params: { page } })

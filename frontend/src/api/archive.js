import api from './axios.js'

export const getArchives = (page = 0, size = 10) =>
  api.get('/api/archives', { params: { page, size } })

export const getArchive = (id) => api.get(`/api/archives/${id}`)

export const createArchive = ({ tmdbId, contentType, rating, review }) =>
  api.post('/api/archives', { tmdbId, contentType, rating, review })

export const updateArchive = (id, { rating, review }) =>
  api.patch(`/api/archives/${id}`, { rating, review })

export const deleteArchive = (id) => api.delete(`/api/archives/${id}`)

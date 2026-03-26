import api from './axios.js'

export const searchMovies = (query, page = 1) =>
  api.get('/api/1/movies/search', { params: { query, page } })

export const searchTv = (query, page = 1) =>
  api.get('/api/1/tv/search', { params: { query, page } })

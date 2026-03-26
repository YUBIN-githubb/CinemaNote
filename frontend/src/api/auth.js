import api from './axios.js'

export const signup = ({ email, nickname, password }) =>
  api.post('/signup', { email, nickname, password })

export const signin = ({ email, password }) =>
  api.post('/signin', { email, password })

export const signout = () => api.post('/signout')

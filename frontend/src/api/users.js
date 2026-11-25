// Only the mediator calls these functions. Components never import this file.
import { jsonRequest, request } from './http'

export const createUser = (user) => request('/api/users/create', jsonRequest('POST', user))

export const loginUser = (credentials) =>
  request('/api/users/login', jsonRequest('POST', credentials))

export const getUsers = () => request('/api/users/getAll')

/** The predetermined security questions: [{ id, text }, ...] */
export const getSecurityQuestions = () => request('/api/security-questions')

/** Who is logged in right now? Rejects with status 401 when there is no valid session. */
export const getSession = () => request('/api/session')

export const logoutSession = () => request('/api/session/logout', { method: 'POST' })

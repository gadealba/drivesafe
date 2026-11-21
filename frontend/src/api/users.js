// Only the mediator calls these functions. Components never import this file.

async function request(url, options) {
  const res = await fetch(url, options)
  if (!res.ok) {
    const problem = await res.json().catch(() => ({}))
    throw new Error(problem.detail || `Request failed (${res.status})`)
  }
  return res.json()
}

const jsonRequest = (method, body) => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
})

export const createUser = (user) => request('/api/users/create', jsonRequest('POST', user))

export const loginUser = (credentials) =>
  request('/api/users/login', jsonRequest('POST', credentials))

export const getUsers = () => request('/api/users/getAll')

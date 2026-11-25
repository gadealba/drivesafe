// Shared fetch helpers. Only files in api/ import this.

export async function request(url, options) {
  const res = await fetch(url, options)
  if (!res.ok) {
    const problem = await res.json().catch(() => ({}))
    const error = new Error(problem.detail || `Request failed (${res.status})`)
    error.status = res.status
    throw error
  }
  return res.status === 204 ? null : res.json()
}

export const jsonRequest = (method, body) => ({
  method,
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
})

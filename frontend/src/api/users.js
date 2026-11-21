export async function createUser(user) {
  const res = await fetch('/api/users/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(user),
  })
  if (!res.ok) {
    const problem = await res.json().catch(() => ({}))
    throw new Error(problem.detail || `Request failed (${res.status})`)
  }
  return res.json()
}

export async function getUsers() {
  const res = await fetch('/api/users/getAll')
  if (!res.ok) throw new Error(`Request failed (${res.status})`)
  return res.json()
}

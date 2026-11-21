import { useEffect, useState } from 'react'
import { getUsers } from '../api/users'

export default function UserList({ refreshKey }) {
  const [users, setUsers] = useState([])

  useEffect(() => {
    getUsers().then(setUsers).catch(console.error)
  }, [refreshKey])

  return (
    <ul>
      {users.map((u) => (
        <li key={u.id}>{u.username} ({u.email})</li>
      ))}
    </ul>
  )
}
import { io, Socket } from 'socket.io-client'

export function connect(userId: string): Socket {
  const url = import.meta.env.VITE_WS_BASE || 'http://localhost:8080'
  
  const socket = io(url, {
    path: '/ws',
    transports: ['websocket'],
    query: { userId },
    reconnection: true,
    reconnectionDelay: 1000,
    reconnectionDelayMax: 5000,
  })

  socket.on('connect', () => {
    console.log('WebSocket Connected:', userId)
  })

  socket.on('disconnect', () => {
    console.log('WebSocket Disconnected')
  })

  socket.on('error', (error) => {
    console.error('WebSocket Error:', error)
  })

  return socket
}


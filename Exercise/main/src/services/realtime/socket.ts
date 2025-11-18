import { io, Socket } from 'socket.io-client'

export function connect(userId: string): Socket {
  // VITE_API_BASE를 기본값으로 사용 (WebSocket도 같은 서버 사용)
  const apiBase = import.meta.env.VITE_API_BASE || 'http://localhost:8081'
  const url = import.meta.env.VITE_WS_BASE || apiBase
  const path = import.meta.env.VITE_WS_PATH || '/socket.io' // 기본 경로는 socket.io 표준 경로
  const enableWs = (import.meta.env.VITE_ENABLE_WS || 'true') === 'true'

  if (!enableWs) {
    // @ts-expect-error - 더미 소켓 (필요 시 호출부에서 null 체크)
    console.warn('WebSocket 비활성화됨(VITE_ENABLE_WS=false). 연결을 건너뜁니다.')
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    return { on: () => {}, emit: () => {}, disconnect: () => {} }
  }
  
  let retryCount = 0
  const maxRetries = Number(import.meta.env.VITE_WS_MAX_RETRIES || 5)

  const socket = io(url, {
    path,
    transports: ['websocket', 'polling'],
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

  socket.io.on('reconnect_attempt', () => {
    retryCount += 1
    console.warn(`WebSocket reconnect attempt ${retryCount}/${maxRetries}`)
    if (retryCount >= maxRetries) {
      console.error('WebSocket: 최대 재시도 횟수 초과. 재접속을 중단합니다.')
      socket.io.opts.reconnection = false
      try { socket.disconnect() } catch {}
    }
  })

  return socket
}


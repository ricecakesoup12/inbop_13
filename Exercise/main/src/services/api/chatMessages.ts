import http from './http'

/**
 * 채팅 메시지 타입
 */
export interface ChatMessage {
  id?: number
  userId: string
  sender: 'user' | 'guardian' // 사용자 또는 보호자(운동 선생님)
  senderName: string // 발신자 이름
  message: string
  timestamp: number
  createdAt?: string
}

/**
 * 채팅 메시지 생성 요청
 */
export interface CreateChatMessageRequest {
  userId: string
  sender: 'user' | 'guardian'
  senderName: string
  message: string
}

/**
 * 특정 사용자의 채팅 메시지 조회
 * @param userId 사용자 ID
 */
export async function getChatMessages(userId: string): Promise<ChatMessage[]> {
  try {
    const response = await http.get<ChatMessage[]>(`/api/chat-messages/user/${userId}`)
    return response.data
  } catch (error) {
    console.error('채팅 메시지 조회 실패:', error)
    throw error
  }
}

/**
 * 채팅 메시지 전송
 * @param request 채팅 메시지 생성 요청
 */
export async function sendChatMessage(request: CreateChatMessageRequest): Promise<ChatMessage> {
  try {
    const response = await http.post<ChatMessage>('/api/chat-messages', {
      ...request,
      timestamp: Date.now()
    })
    return response.data
  } catch (error) {
    console.error('채팅 메시지 전송 실패:', error)
    throw error
  }
}

/**
 * 채팅 메시지 삭제
 * @param messageId 메시지 ID
 */
export async function deleteChatMessage(messageId: number): Promise<void> {
  try {
    await http.delete(`/api/chat-messages/${messageId}`)
  } catch (error) {
    console.error('채팅 메시지 삭제 실패:', error)
    throw error
  }
}

/**
 * 모든 채팅 메시지 조회 (보호자용 - 전체 대화 내역)
 */
export async function getAllChatMessages(): Promise<ChatMessage[]> {
  try {
    const response = await http.get<ChatMessage[]>('/api/chat-messages')
    return response.data
  } catch (error) {
    console.error('전체 채팅 메시지 조회 실패:', error)
    throw error
  }
}


export interface ChatMessage {
  id?: number
  sessionId: string
  role: 'user' | 'assistant'
  content: string
  tokens?: number
  createdAt?: string
  isStreaming?: boolean
}

export interface ConversationHistory {
  sessionId: string
  messages: ChatMessage[]
  totalMessages: number
}

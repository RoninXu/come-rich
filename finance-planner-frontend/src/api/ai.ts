import request from '@/utils/request'
import { getToken } from '@/utils/auth'
import type { ConversationHistory } from '@/types/ai'
import type { ApiResponse } from '@/types/api'

/**
 * Stream AI chat response using fetch + ReadableStream.
 * Yields text chunks as they arrive via SSE.
 */
export async function* streamChat(
  message: string,
  sessionId?: string
): AsyncGenerator<{ sessionId?: string; content?: string; error?: string; done?: boolean }> {
  const token = getToken()
  const params = new URLSearchParams({ message })
  if (sessionId) {
    params.append('sessionId', sessionId)
  }

  const response = await fetch(`/api/ai/chat-stream?${params.toString()}`, {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${token}`,
      Accept: 'text/event-stream'
    }
  })

  if (!response.ok) {
    yield { error: 'AI 服务请求失败' }
    return
  }

  const reader = response.body?.getReader()
  if (!reader) {
    yield { error: '无法读取响应流' }
    return
  }

  const decoder = new TextDecoder()
  let buffer = ''

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed || !trimmed.startsWith('data:')) continue

        const dataStr = trimmed.substring(5).trim()
        if (dataStr === '[DONE]') {
          yield { done: true }
          return
        }

        try {
          const data = JSON.parse(dataStr)
          if (data.error) {
            yield { error: data.error }
            return
          }
          yield { sessionId: data.sessionId, content: data.content }
        } catch {
          // Skip non-JSON lines
        }
      }
    }
  } finally {
    reader.releaseLock()
  }
}

export function getConversationHistory(sessionId: string) {
  return request.get<ApiResponse<ConversationHistory>>('/ai/history', {
    params: { sessionId }
  })
}

export function getRemainingChats() {
  return request.get<ApiResponse<number>>('/ai/remaining')
}

export function getProviders() {
  return request.get<ApiResponse<string[]>>('/ai/providers')
}

export function getCurrentProvider() {
  return request.get<ApiResponse<string>>('/ai/provider')
}

export function switchProvider(name: string) {
  return request.put<ApiResponse<void>>('/ai/provider', null, {
    params: { name }
  })
}

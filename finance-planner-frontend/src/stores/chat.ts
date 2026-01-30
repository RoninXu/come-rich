import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  streamChat,
  getRemainingChats,
  getConversationHistory,
  getProviders,
  getCurrentProvider,
  switchProvider as switchProviderApi
} from '@/api/ai'
import type { ChatMessage } from '@/types/ai'

export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])
  const currentSessionId = ref<string | null>(null)
  const isStreaming = ref(false)
  const remainingChats = ref<number>(10)
  const error = ref<string | null>(null)
  const currentProvider = ref<string>('')
  const providers = ref<string[]>([])

  async function sendMessage(content: string) {
    error.value = null

    // Add user message
    const userMessage: ChatMessage = {
      sessionId: currentSessionId.value || '',
      role: 'user',
      content
    }
    messages.value.push(userMessage)

    // Add placeholder for AI response
    const aiMessage: ChatMessage = {
      sessionId: currentSessionId.value || '',
      role: 'assistant',
      content: '',
      isStreaming: true
    }
    messages.value.push(aiMessage)

    isStreaming.value = true

    try {
      const generator = streamChat(content, currentSessionId.value || undefined)
      for await (const chunk of generator) {
        if (chunk.error) {
          error.value = chunk.error
          aiMessage.content = chunk.error
          aiMessage.isStreaming = false
          break
        }
        if (chunk.done) {
          aiMessage.isStreaming = false
          break
        }
        if (chunk.sessionId && !currentSessionId.value) {
          currentSessionId.value = chunk.sessionId
          userMessage.sessionId = chunk.sessionId
          aiMessage.sessionId = chunk.sessionId
        }
        if (chunk.content) {
          aiMessage.content += chunk.content
        }
      }
    } catch (e: any) {
      error.value = e.message || 'Failed to get AI response'
      aiMessage.content = error.value || 'Error'
      aiMessage.isStreaming = false
    } finally {
      isStreaming.value = false
      aiMessage.isStreaming = false
    }
  }

  async function loadHistory(sessionId: string) {
    try {
      const response = await getConversationHistory(sessionId)
      const history = response.data.data
      currentSessionId.value = history.sessionId
      messages.value = history.messages.map((msg) => ({
        ...msg,
        isStreaming: false
      }))
    } catch (e: any) {
      error.value = e.message || 'Failed to load history'
    }
  }

  async function fetchRemainingChats() {
    try {
      const response = await getRemainingChats()
      remainingChats.value = response.data.data
    } catch {
      // Silently fail
    }
  }

  function startNewSession() {
    messages.value = []
    currentSessionId.value = null
    error.value = null
  }

  async function fetchProviders() {
    try {
      const [providersRes, currentRes] = await Promise.all([
        getProviders(),
        getCurrentProvider()
      ])
      providers.value = providersRes.data.data
      currentProvider.value = currentRes.data.data
    } catch {
      // Silently fail
    }
  }

  async function switchProviderAction(name: string) {
    try {
      await switchProviderApi(name)
      currentProvider.value = name
    } catch (e: any) {
      error.value = e.message || 'Failed to switch provider'
    }
  }

  return {
    messages,
    currentSessionId,
    isStreaming,
    remainingChats,
    error,
    currentProvider,
    providers,
    sendMessage,
    loadHistory,
    fetchRemainingChats,
    startNewSession,
    fetchProviders,
    switchProviderAction
  }
})

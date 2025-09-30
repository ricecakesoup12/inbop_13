<template>
  <div class="chat-container">
    <div class="chat-header">
      <h3>🤖 운동 챗봇</h3>
      <div class="voice-indicator" :class="{ active: isListening }">
        <span v-if="isListening">🎤 듣는 중...</span>
        <span v-else>🎤 음성 인식</span>
      </div>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div 
        v-for="message in messages" 
        :key="message.id"
        :class="['message', message.type]"
      >
        <div class="message-avatar">
          <span v-if="message.type === 'bot'">🤖</span>
          <span v-else>👤</span>
        </div>
        <div class="message-content">
          <div class="message-text">{{ message.text }}</div>
          <div class="message-time">{{ formatTime(message.timestamp) }}</div>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <input 
        v-model="inputMessage"
        @keypress.enter="sendMessage"
        placeholder="메시지를 입력하거나 음성으로 말하세요..."
        :disabled="isListening"
      />
      <button 
        @click="toggleVoiceRecognition" 
        :class="['voice-btn', { active: isListening }]"
        :disabled="isProcessing"
      >
        {{ isListening ? '⏹️' : '🎤' }}
      </button>
      <button 
        @click="sendMessage" 
        :disabled="!inputMessage.trim() || isProcessing"
        class="send-btn"
      >
        전송
      </button>
    </div>
    
    <div class="quick-actions">
      <button 
        v-for="action in quickActions" 
        :key="action.text"
        @click="sendQuickMessage(action.text)"
        class="quick-action-btn"
        :disabled="isProcessing"
      >
        {{ action.icon }} {{ action.text }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatBot',
  data() {
    return {
      messages: [
        {
          id: 1,
          type: 'bot',
          text: '안녕하세요! 운동 챗봇입니다. 오늘 어떤 운동을 도와드릴까요?',
          timestamp: new Date()
        }
      ],
      inputMessage: '',
      isListening: false,
      isProcessing: false,
      quickActions: [
        { icon: '🏃‍♂️', text: '운동 시작' },
        { icon: '💓', text: '심박수 확인' },
        { icon: '🫁', text: '산소포화도 확인' },
        { icon: '📊', text: '운동 강도 조절' },
        { icon: '⏸️', text: '운동 일시정지' },
        { icon: '🆘', text: '도움 요청' }
      ]
    }
  },
  mounted() {
    this.scrollToBottom()
  },
  methods: {
    sendMessage() {
      if (!this.inputMessage.trim() || this.isProcessing) return
      
      const userMessage = {
        id: Date.now(),
        type: 'user',
        text: this.inputMessage,
        timestamp: new Date()
      }
      
      this.messages.push(userMessage)
      this.isProcessing = true
      
      // 시뮬레이션된 응답 지연
      setTimeout(() => {
        this.generateBotResponse(userMessage.text)
        this.isProcessing = false
      }, 1000)
      
      this.inputMessage = ''
      this.scrollToBottom()
    },
    
    sendQuickMessage(text) {
      this.inputMessage = text
      this.sendMessage()
    },
    
    generateBotResponse(userInput) {
      const responses = this.getResponses(userInput)
      const response = responses[Math.floor(Math.random() * responses.length)]
      
      const botMessage = {
        id: Date.now(),
        type: 'bot',
        text: response,
        timestamp: new Date()
      }
      
      this.messages.push(botMessage)
      this.scrollToBottom()
    },
    
    getResponses(input) {
      const lowerInput = input.toLowerCase()
      
      if (lowerInput.includes('운동') && lowerInput.includes('시작')) {
        return [
          '좋습니다! 운동을 시작하겠습니다. 먼저 준비운동부터 해보세요.',
          '운동 시작! 안전을 위해 천천히 시작하시고, 몸의 상태를 확인해주세요.',
          '운동을 시작합니다. 심박수와 산소포화도를 모니터링하면서 진행하겠습니다.'
        ]
      } else if (lowerInput.includes('심박수')) {
        return [
          '현재 심박수는 정상 범위입니다. 계속 운동을 진행하셔도 됩니다.',
          '심박수가 약간 높아졌네요. 운동 강도를 조절해보겠습니다.',
          '심박수 모니터링 중입니다. 이상이 있으면 알려드리겠습니다.'
        ]
      } else if (lowerInput.includes('산소포화도')) {
        return [
          '산소포화도가 양호합니다. 안전하게 운동을 계속하세요.',
          '산소포화도가 조금 낮아졌습니다. 휴식을 취해보세요.',
          '산소포화도를 지속적으로 모니터링하고 있습니다.'
        ]
      } else if (lowerInput.includes('강도')) {
        return [
          '운동 강도를 조절하겠습니다. 현재 상태에 맞게 조정해드릴게요.',
          '생체 신호에 따라 운동 강도를 자동으로 조절했습니다.',
          '운동 강도가 적절한지 확인해보겠습니다.'
        ]
      } else if (lowerInput.includes('일시정지') || lowerInput.includes('중단')) {
        return [
          '운동을 일시정지합니다. 휴식을 취하세요.',
          '운동을 중단했습니다. 몸의 상태를 확인해보세요.',
          '안전을 위해 운동을 일시정지합니다.'
        ]
      } else if (lowerInput.includes('도움') || lowerInput.includes('help')) {
        return [
          '도움이 필요하시군요! 보호자에게 연락을 취하겠습니다.',
          '긴급 상황입니다. 즉시 보호자에게 알림을 보내겠습니다.',
          '도움 요청을 받았습니다. 응급 연락처에 알림을 전송했습니다.'
        ]
      } else {
        return [
          '네, 이해했습니다. 더 구체적으로 말씀해주시면 도움을 드릴 수 있습니다.',
          '운동과 관련해서 도움이 필요한 것이 있으시면 언제든 말씀해주세요.',
          '생체 신호를 모니터링하면서 안전한 운동을 도와드리겠습니다.'
        ]
      }
    },
    
    toggleVoiceRecognition() {
      if (this.isListening) {
        this.stopVoiceRecognition()
      } else {
        this.startVoiceRecognition()
      }
    },
    
    startVoiceRecognition() {
      this.isListening = true
      // 실제 음성 인식 구현은 Web Speech API를 사용
      if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
        const recognition = new SpeechRecognition()
        
        recognition.lang = 'ko-KR'
        recognition.continuous = false
        recognition.interimResults = false
        
        recognition.onresult = (event) => {
          const transcript = event.results[0][0].transcript
          this.inputMessage = transcript
          this.sendMessage()
        }
        
        recognition.onend = () => {
          this.isListening = false
        }
        
        recognition.onerror = () => {
          this.isListening = false
          alert('음성 인식에 오류가 발생했습니다.')
        }
        
        recognition.start()
      } else {
        alert('이 브라우저는 음성 인식을 지원하지 않습니다.')
        this.isListening = false
      }
    },
    
    stopVoiceRecognition() {
      this.isListening = false
    },
    
    formatTime(timestamp) {
      return timestamp.toLocaleTimeString('ko-KR', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    },
    
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.messagesContainer
        container.scrollTop = container.scrollHeight
      })
    }
  }
}
</script>

<style scoped>
.chat-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  height: 400px;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.chat-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.2rem;
}

.voice-indicator {
  padding: 5px 10px;
  border-radius: 15px;
  background-color: #f0f0f0;
  font-size: 0.8rem;
  color: #666;
  transition: all 0.3s ease;
}

.voice-indicator.active {
  background-color: #ffebee;
  color: #f44336;
  animation: pulse 1s infinite;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.message {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background-color: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  margin: 0 10px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background-color: #3ec26e;
  color: white;
}

.message-content {
  max-width: 70%;
  background-color: white;
  padding: 10px 15px;
  border-radius: 15px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.message.user .message-content {
  background-color: #3ec26e;
  color: white;
}

.message-text {
  margin-bottom: 5px;
  line-height: 1.4;
}

.message-time {
  font-size: 0.7rem;
  opacity: 0.7;
}

.chat-input {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.chat-input input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s ease;
}

.chat-input input:focus {
  border-color: #3ec26e;
}

.voice-btn, .send-btn {
  padding: 10px 15px;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.voice-btn {
  background-color: #f8f9fa;
  color: #666;
}

.voice-btn.active {
  background-color: #f44336;
  color: white;
}

.voice-btn:hover:not(:disabled) {
  background-color: #e9ecef;
}

.voice-btn.active:hover {
  background-color: #d32f2f;
}

.send-btn {
  background-color: #3ec26e;
  color: white;
}

.send-btn:hover:not(:disabled) {
  background-color: #35a15d;
}

.send-btn:disabled, .voice-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-action-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 15px;
  background-color: white;
  color: #666;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.3s ease;
}

.quick-action-btn:hover:not(:disabled) {
  background-color: #f8f9fa;
  border-color: #3ec26e;
  color: #3ec26e;
}

.quick-action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

@media (max-width: 768px) {
  .chat-container {
    height: 350px;
  }
  
  .quick-actions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
  }
  
  .quick-action-btn {
    font-size: 0.7rem;
    padding: 6px 10px;
  }
}
</style>

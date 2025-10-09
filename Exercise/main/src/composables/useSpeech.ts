import { ref } from 'vue'

export function useSpeech() {
  const listening = ref(false)
  const transcript = ref('')
  const error = ref<string | null>(null)
  
  let recognition: any = null

  const start = () => {
    const SpeechRecognition = (window as any).webkitSpeechRecognition || (window as any).SpeechRecognition
    
    if (!SpeechRecognition) {
      error.value = '음성 인식을 지원하지 않는 브라우저입니다.'
      alert('음성 인식을 지원하지 않는 브라우저입니다.')
      return
    }

    recognition = new SpeechRecognition()
    recognition.lang = 'ko-KR'
    recognition.continuous = false
    recognition.interimResults = false

    recognition.onstart = () => {
      listening.value = true
      error.value = null
    }

    recognition.onend = () => {
      listening.value = false
    }

    recognition.onresult = (event: any) => {
      const text = event.results[0][0].transcript
      transcript.value = text
      console.log('Speech recognized:', text)
    }

    recognition.onerror = (event: any) => {
      error.value = event.error
      listening.value = false
    }

    recognition.start()
  }

  const stop = () => {
    if (recognition) {
      recognition.stop()
      listening.value = false
    }
  }

  return {
    listening,
    transcript,
    error,
    start,
    stop,
  }
}


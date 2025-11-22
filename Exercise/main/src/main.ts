import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// CSS 파일 import - 순서 중요!
// 1. Tailwind CSS (base, components, utilities 포함)
import './assets/styles/tailwind.css'
// 2. 기본 스타일 (CSS 변수 등)
import './assets/styles/base.css'
// 3. 커스텀 컴포넌트 스타일 (Tailwind 이후에 로드하여 우선순위 확보)
import './assets/styles/components.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')


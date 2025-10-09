<template>
  <div class="max-w-2xl mx-auto">
    <h2 class="text-2xl font-semibold text-text-main mb-6 font-gowun">설문 보내기</h2>

    <div class="bg-white rounded-xl shadow-soft border border-gray-200 p-6">
      <p class="text-text-sub mb-6 font-gowun">
        사용자에게 건강 설문을 요청합니다. 전송 후 사용자가 설문을 완료하면 결과를 확인할 수 있습니다.
      </p>

      <div class="space-y-4">
        <div>
          <label class="block text-sm font-semibold text-text-main mb-2 font-gowun">요청 메시지 (선택)</label>
          <textarea
            v-model="message"
            class="w-full p-3 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun"
            rows="4"
            placeholder="사용자에게 전달할 메시지를 입력하세요"
          />
        </div>

        <div class="flex gap-3 justify-end pt-4">
          <AppButton variant="ghost" @click="$router.back()">취소</AppButton>
          <AppButton variant="solid" @click="sendSurvey" :disabled="sending">
            {{ sending ? '전송 중...' : '설문 요청 전송' }}
          </AppButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createSurveyRequest } from '@/services/api/surveyRequests'
import AppButton from '@/components/common/AppButton.vue'

const route = useRoute()
const router = useRouter()

const userId = route.params.id as string
const message = ref('건강 설문을 작성해주세요.')
const sending = ref(false)

const sendSurvey = async () => {
  sending.value = true

  try {
    // 설문 요청 전송
    await createSurveyRequest(userId, message.value || '건강 설문을 작성해주세요.')

    alert('설문 요청이 전송되었습니다!')
    router.push(`/guardian/users/${userId}`)
  } catch (error) {
    console.error(error)
    alert('설문 요청 전송에 실패했습니다.')
  } finally {
    sending.value = false
  }
}
</script>


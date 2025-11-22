<template>
  <section class="GuardianHomePage">
    <div class="GuardianHomeHeader">
      <h2 class="GuardianHomeTitle">사용자 리스트</h2>
      <div class="GuardianHomeActions">
        <AppButton variant="outline" @click="showLocationMap = true" class="ViewLocationMapButton">
          <svg class="ViewLocationMapIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          실시간 위치 보기
        </AppButton>
        <AppButton @click="showAddUser = true" class="AddUserButton">
          <svg class="AddUserIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
          </svg>
          사용자 추가
        </AppButton>
      </div>
    </div>
    
    <div v-if="loading" class="GuardianHomeLoading">
      <p>불러오는 중...</p>
    </div>

    <div v-else-if="error" class="GuardianHomeError">
      <p>{{ error }}</p>
      <AppButton @click="loadUsers" class="RetryButton">다시 시도</AppButton>
    </div>

    <div v-else-if="users.length === 0" class="GuardianHomeEmpty">
      <p>등록된 사용자가 없습니다.</p>
    </div>

    <div v-else class="GuardianHomeUserList">
      <UserListItem
        v-for="user in users"
        :key="user.id"
        :user="user"
        :userLocation="userLocations[user.id]"
        @open="openUser(user.id)"
        @delete="confirmDelete(user)"
      />
    </div>

    <ConfirmDialog
      :open="deleteDialog.open"
      :title="deleteDialog.title"
      :message="deleteDialog.message"
      @confirm="handleDelete"
      @cancel="deleteDialog.open = false"
    />

    <!-- 실시간 위치 보기 모달 -->
    <AppModal :open="showLocationMap" title="전체 사용자 실시간 위치" @close="showLocationMap = false" :wide="true">
      <GuardianAllUsersMap :users="users" />
    </AppModal>

    <!-- 사용자 추가 모달 -->
    <AppModal :open="showAddUser" title="사용자 추가" @close="closeAddUser">
      <form @submit.prevent="handleAddUser" class="AddUserForm">
        <!-- 사용자 사진 -->
        <div class="AddUserPhotoSection">
          <label class="AddUserPhotoLabel">
            사용자 사진
          </label>
          <div class="AddUserPhotoContent">
            <!-- 사진 미리보기 -->
            <div class="AddUserPhotoPreview">
              <img
                v-if="newUser.faceUrl"
                :src="newUser.faceUrl"
                alt="프로필 미리보기"
                class="AddUserPhotoPreviewImage"
              />
              <svg
                v-else
                class="AddUserPhotoPlaceholderIcon"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            
            <!-- 파일 입력 (숨김) -->
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              @change="handleImageSelect"
              class="AddUserPhotoFileInput"
            />
            
            <!-- 파일 선택 버튼 -->
            <AppButton
              type="button"
              variant="outline"
              class="SelectPhotoButton"
              @click="fileInputRef?.click()"
            >
              <svg class="SelectPhotoIcon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
              </svg>
              {{ newUser.faceUrl ? '사진 변경' : '사진 선택' }}
            </AppButton>
            
            <!-- 사진 삭제 버튼 -->
            <button
              v-if="newUser.faceUrl"
              type="button"
              @click="newUser.faceUrl = ''"
              class="DeletePhotoButton"
            >
              사진 삭제
            </button>
          </div>
        </div>

        <!-- 사용자 코드 -->
        <div class="AddUserCodeField">
          <label class="AddUserCodeLabel">
            사용자 코드 <span class="AddUserRequired">*</span>
            <span class="AddUserCodeHint">(영문+숫자 4자리, 예: AB12)</span>
          </label>
          <input
            v-model="newUser.userCode"
            type="text"
            required
            maxlength="4"
            pattern="[A-Za-z0-9]{4}"
            placeholder="예: AB12"
            class="AddUserCodeInput"
            @input="newUser.userCode = newUser.userCode.toUpperCase()"
          />
          <p v-if="codeError" class="AddUserCodeError">{{ codeError }}</p>
        </div>

        <!-- 이름 -->
        <div class="AddUserNameField">
          <label class="AddUserNameLabel">
            이름 <span class="AddUserRequired">*</span>
          </label>
          <input
            v-model="newUser.name"
            type="text"
            required
            placeholder="이름을 입력하세요"
            class="AddUserNameInput"
          />
        </div>

        <!-- 성별 -->
        <div class="AddUserGenderField">
          <label class="AddUserGenderLabel">
            성별 <span class="AddUserRequired">*</span>
          </label>
          <div class="AddUserGenderOptions">
            <label
              v-for="g in ['남성', '여성', '기타']"
              :key="g"
              :class="[
                'AddUserGenderOption',
                newUser.gender === g
                  ? 'AddUserGenderOptionActive'
                  : 'AddUserGenderOptionInactive'
              ]"
            >
              <input type="radio" :value="g" v-model="newUser.gender" class="AddUserGenderRadio" required />
              <span>{{ g }}</span>
            </label>
          </div>
        </div>

        <!-- 나이 -->
        <div class="AddUserAgeField">
          <label class="AddUserAgeLabel">
            나이 <span class="AddUserRequired">*</span>
          </label>
          <input
            v-model.number="newUser.age"
            type="number"
            required
            placeholder="나이"
            class="AddUserAgeInput"
          />
        </div>

        <!-- 키 -->
        <div class="AddUserHeightField">
          <label class="AddUserHeightLabel">
            키 (cm) <span class="AddUserRequired">*</span>
          </label>
          <input
            v-model.number="newUser.height"
            type="number"
            required
            placeholder="키"
            class="AddUserHeightInput"
          />
        </div>

        <!-- 몸무게 -->
        <div class="AddUserWeightField">
          <label class="AddUserWeightLabel">
            몸무게 (kg)
          </label>
          <input
            v-model.number="newUser.weight"
            type="number"
            placeholder="몸무게"
            class="AddUserWeightInput"
          />
        </div>

        <!-- 보호자 연락처 -->
        <div class="AddUserGuardianPhoneField">
          <label class="AddUserGuardianPhoneLabel">
            보호자 연락처
            <span class="AddUserGuardianPhoneHint">(긴급 연락용)</span>
          </label>
          <input
            v-model="newUser.guardianPhone"
            type="tel"
            placeholder="010-1234-5678"
            pattern="[0-9]{2,3}-[0-9]{3,4}-[0-9]{4}"
            class="AddUserGuardianPhoneInput"
          />
          <p class="AddUserGuardianPhoneExample">예: 010-1234-5678</p>
        </div>

        <div class="AddUserActions">
          <AppButton type="button" variant="ghost" @click="closeAddUser" class="AddUserCancelButton">
            취소
          </AppButton>
          <AppButton type="submit" :disabled="addLoading" class="AddUserSubmitButton">
            {{ addLoading ? '등록 중...' : '등록하기' }}
          </AppButton>
        </div>
      </form>
    </AppModal>
  </section>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUsersStore } from '@/stores/users.store'
import { checkUserCode } from '@/services/api/users'
import { getAllLocations } from '@/services/api/locations'
import type { LocationDto } from '@/services/api/locations'
import UserListItem from '@/components/user/UserListItem.vue'
import GuardianAllUsersMap from '@/components/map/GuardianAllUsersMap.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import AppButton from '@/components/common/AppButton.vue'
import AppModal from '@/components/common/AppModal.vue'
import type { User } from '@/types/user'

const router = useRouter()
const usersStore = useUsersStore()

const users = computed(() => usersStore.list)
const loading = computed(() => usersStore.loading)
const error = computed(() => usersStore.error)

const deleteDialog = reactive({
  open: false,
  title: '',
  message: '',
  userId: '',
})

const showAddUser = ref(false)
const showLocationMap = ref(false)
const addLoading = ref(false)
const codeError = ref('')
const fileInputRef = ref<HTMLInputElement | null>(null)
const userLocations = ref<Record<string, { lat: number; lng: number } | null>>({})
let locationUpdateInterval: number | null = null
const newUser = reactive({
  userCode: '',
  name: '',
  gender: '',
  age: null as number | null,
  height: null as number | null,
  weight: null as number | null,
  guardianPhone: '',
  faceUrl: '',
})

// 이미지 선택 핸들러
const handleImageSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  // 파일 크기 체크 (5MB 제한)
  if (file.size > 5 * 1024 * 1024) {
    alert('파일 크기는 5MB 이하여야 합니다.')
    target.value = '' // 파일 입력 초기화
    return
  }
  
  // 이미지 파일 타입 체크
  if (!file.type.startsWith('image/')) {
    alert('이미지 파일만 업로드 가능합니다.')
    target.value = '' // 파일 입력 초기화
    return
  }
  
  // FileReader로 Base64 변환
  const reader = new FileReader()
  reader.onload = (e) => {
    const result = e.target?.result as string
    // Base64 결과가 너무 큰 경우 압축 시도 (100KB 정도로 제한)
    if (result.length > 100000) {
      // 이미지 크기 줄이기
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        // 최대 크기 조정 (800x800 또는 원본 중 작은 것)
        const maxSize = 800
        let width = img.width
        let height = img.height
        
        if (width > maxSize || height > maxSize) {
          if (width > height) {
            height = (height * maxSize) / width
            width = maxSize
          } else {
            width = (width * maxSize) / height
            height = maxSize
          }
        }
        
        canvas.width = width
        canvas.height = height
        const ctx = canvas.getContext('2d')
        if (ctx) {
          ctx.drawImage(img, 0, 0, width, height)
          const compressedResult = canvas.toDataURL('image/jpeg', 0.8)
          newUser.faceUrl = compressedResult
        } else {
          alert('이미지 처리에 실패했습니다.')
        }
      }
      img.onerror = () => {
        alert('이미지 로드에 실패했습니다.')
      }
      img.src = result
    } else {
      newUser.faceUrl = result
    }
  }
  reader.onerror = () => {
    alert('이미지 로드에 실패했습니다.')
    target.value = '' // 파일 입력 초기화
  }
  reader.readAsDataURL(file)
}

const loadUsers = () => {
  usersStore.fetchList()
}

// 위치 정보 업데이트 함수
const updateUserLocations = async () => {
  try {
    const locations = await getAllLocations()
    
    // 모든 사용자에 대해 위치 정보 초기화
    const newLocations: Record<string, { lat: number; lng: number } | null> = {}
    users.value.forEach(user => {
      newLocations[user.id] = null
    })
    
    // 위치 데이터 매핑
    locations.forEach((location: LocationDto) => {
      const userId = String(location.userId)
      const user = users.value.find(u => String(u.id) === userId)
      if (user) {
        newLocations[user.id] = {
          lat: location.latitude,
          lng: location.longitude
        }
      }
    })
    
    userLocations.value = newLocations
  } catch (error) {
    console.error('위치 정보 업데이트 실패:', error)
  }
}

onMounted(async () => {
  await loadUsers()
  
  // 초기 위치 로드
  await updateUserLocations()
  
  // 주기적으로 위치 업데이트 (5초마다)
  locationUpdateInterval = window.setInterval(updateUserLocations, 5000)
})

onBeforeUnmount(() => {
  if (locationUpdateInterval) {
    clearInterval(locationUpdateInterval)
  }
})

const openUser = (id: string) => {
  router.push(`/guardian/users/${id}`)
}

const confirmDelete = (user: User) => {
  deleteDialog.open = true
  deleteDialog.title = '회원 삭제'
  deleteDialog.message = `${user.name}님을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`
  deleteDialog.userId = user.id
}

const handleDelete = async () => {
  await usersStore.deleteUser(deleteDialog.userId)
  deleteDialog.open = false
}

const closeAddUser = () => {
  showAddUser.value = false
  codeError.value = ''
  // 폼 초기화
  newUser.userCode = ''
  newUser.name = ''
  newUser.gender = ''
  newUser.age = null
  newUser.height = null
  newUser.weight = null
  newUser.guardianPhone = ''
  newUser.faceUrl = ''
}

const handleAddUser = async () => {
  // 코드 형식 검증
  if (!/^[A-Za-z0-9]{4}$/.test(newUser.userCode)) {
    codeError.value = '코드는 영문+숫자 4자리여야 합니다.'
    return
  }

  addLoading.value = true
  codeError.value = ''

  try {
    // 코드 중복 확인
    const exists = await checkUserCode(newUser.userCode)
    if (exists) {
      codeError.value = '이미 사용 중인 코드입니다.'
      addLoading.value = false
      return
    }

    const userData: any = {
      userCode: newUser.userCode,
      name: newUser.name,
      gender: newUser.gender as '남성' | '여성' | '기타',
      age: newUser.age!,
      height: newUser.height!,
    }

    // 선택적 필드 - 빈 문자열은 undefined로 변환
    if (newUser.weight && newUser.weight > 0) {
      userData.weight = newUser.weight
    }
    if (newUser.guardianPhone && newUser.guardianPhone.trim()) {
      userData.guardianPhone = newUser.guardianPhone.trim()
    }
    if (newUser.faceUrl && newUser.faceUrl.trim()) {
      userData.faceUrl = newUser.faceUrl.trim()
    }
    
    // 디버깅: 전송할 데이터 확인
    console.log('=== 회원 추가 - 전송 데이터 ===')
    console.log('보호자 연락처:', userData.guardianPhone)
    console.log('전체 데이터:', JSON.stringify(userData, null, 2))

    // 사용자 등록
    const result = await usersStore.createUser(userData)
    
    console.log('=== 회원 추가 - 등록 결과 ===')
    console.log('등록된 사용자:', result)
    console.log('등록된 보호자 연락처:', result?.guardianPhone)

    alert(`${newUser.name}님이 등록되었습니다!\n사용자 코드: ${newUser.userCode}`)
    closeAddUser()
    loadUsers()
  } catch (error: any) {
    console.error('등록 실패:', error)
    
    // 더 구체적인 에러 메시지 표시
    let errorMessage = '등록에 실패했습니다. 다시 시도해주세요.'
    
    if (error?.response) {
      const status = error.response.status
      const data = error.response.data
      
      // 백엔드에서 보낸 에러 메시지
      if (typeof data === 'string') {
        errorMessage = data
      } else if (data?.message) {
        errorMessage = data.message
      } else if (data?.error) {
        errorMessage = data.error
      } else if (status === 400) {
        errorMessage = '입력 정보가 올바르지 않습니다. 모든 필수 항목을 확인해주세요.'
      } else if (status === 500) {
        errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.'
      }
    } else if (error?.message) {
      errorMessage = error.message
    } else if (error?.code === 'ECONNABORTED') {
      errorMessage = '요청 시간이 초과되었습니다. 네트워크 연결을 확인해주세요.'
    }
    
    alert(`등록 실패: ${errorMessage}`)
  } finally {
    addLoading.value = false
  }
}
</script>


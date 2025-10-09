<template>
  <section>
    <div class="flex justify-between items-center mb-6">
      <h2 class="text-2xl font-semibold text-text-main font-gowun">사용자 리스트</h2>
      <AppButton @click="showAddUser = true">
        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        사용자 추가
      </AppButton>
    </div>
    
    <div v-if="loading" class="text-center py-12">
      <p class="text-text-sub font-gowun">불러오는 중...</p>
    </div>

    <div v-else-if="error" class="text-center py-12">
      <p class="text-red-600 font-gowun">{{ error }}</p>
      <AppButton @click="loadUsers" class="mt-4">다시 시도</AppButton>
    </div>

    <div v-else-if="users.length === 0" class="text-center py-12">
      <p class="text-text-sub font-gowun">등록된 사용자가 없습니다.</p>
    </div>

    <div v-else class="space-y-4">
      <UserListItem
        v-for="user in users"
        :key="user.id"
        :user="user"
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

    <!-- 사용자 추가 모달 -->
    <AppModal :open="showAddUser" title="사용자 추가" @close="closeAddUser">
      <form @submit.prevent="handleAddUser" class="space-y-4">
        <!-- 사용자 코드 -->
        <div>
          <label class="block font-semibold mb-2 font-gowun">
            사용자 코드 <span class="text-red-500">*</span>
            <span class="text-xs text-text-sub ml-2">(영문+숫자 4자리, 예: AB12)</span>
          </label>
          <input
            v-model="newUser.userCode"
            type="text"
            required
            maxlength="4"
            pattern="[A-Za-z0-9]{4}"
            placeholder="예: AB12"
            class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun uppercase"
            @input="newUser.userCode = newUser.userCode.toUpperCase()"
          />
          <p v-if="codeError" class="text-xs text-red-500 mt-1 font-gowun">{{ codeError }}</p>
        </div>

        <!-- 이름 -->
        <div>
          <label class="block font-semibold mb-2 font-gowun">
            이름 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="newUser.name"
            type="text"
            required
            placeholder="이름을 입력하세요"
            class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun"
          />
        </div>

        <!-- 성별 -->
        <div>
          <label class="block font-semibold mb-2 font-gowun">
            성별 <span class="text-red-500">*</span>
          </label>
          <div class="grid grid-cols-3 gap-3">
            <label
              v-for="g in ['남성', '여성', '기타']"
              :key="g"
              :class="[
                'flex items-center justify-center p-3 border-2 rounded-xl cursor-pointer transition-all font-gowun',
                newUser.gender === g
                  ? 'bg-primary/10 border-primary text-primary font-semibold'
                  : 'border-gray-200 hover:border-gray-300'
              ]"
            >
              <input type="radio" :value="g" v-model="newUser.gender" class="hidden" required />
              <span>{{ g }}</span>
            </label>
          </div>
        </div>

        <!-- 나이 -->
        <div>
          <label class="block font-semibold mb-2 font-gowun">
            나이 <span class="text-red-500">*</span>
          </label>
          <input
            v-model.number="newUser.age"
            type="number"
            required
            placeholder="나이"
            class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun"
          />
        </div>

        <!-- 키 -->
        <div>
          <label class="block font-semibold mb-2 font-gowun">
            키 (cm) <span class="text-red-500">*</span>
          </label>
          <input
            v-model.number="newUser.height"
            type="number"
            required
            placeholder="키"
            class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun"
          />
        </div>

        <!-- 몸무게 -->
        <div>
          <label class="block font-semibold mb-2 font-gowun">
            몸무게 (kg)
          </label>
          <input
            v-model.number="newUser.weight"
            type="number"
            placeholder="몸무게"
            class="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:border-primary outline-none font-gowun"
          />
        </div>

        <div class="flex gap-3 pt-4">
          <AppButton type="button" variant="ghost" @click="closeAddUser" class="flex-1">
            취소
          </AppButton>
          <AppButton type="submit" :disabled="addLoading" class="flex-1">
            {{ addLoading ? '등록 중...' : '등록하기' }}
          </AppButton>
        </div>
      </form>
    </AppModal>
  </section>
</template>

<script setup lang="ts">
import { onMounted, computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUsersStore } from '@/stores/users.store'
import { checkUserCode } from '@/services/api/users'
import UserListItem from '@/components/user/UserListItem.vue'
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
const addLoading = ref(false)
const codeError = ref('')
const newUser = reactive({
  userCode: '',
  name: '',
  gender: '',
  age: null as number | null,
  height: null as number | null,
  weight: null as number | null,
})

const loadUsers = () => {
  usersStore.fetchList()
}

onMounted(() => {
  loadUsers()
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

    const userData = {
      userCode: newUser.userCode,
      name: newUser.name,
      gender: newUser.gender as '남성' | '여성' | '기타',
      age: newUser.age!,
      height: newUser.height!,
      weight: newUser.weight || undefined,
    }

    // 사용자 등록
    await usersStore.createUser(userData)

    alert(`${newUser.name}님이 등록되었습니다!\n사용자 코드: ${newUser.userCode}`)
    closeAddUser()
    loadUsers()
  } catch (error) {
    console.error('등록 실패:', error)
    alert('등록에 실패했습니다. 다시 시도해주세요.')
  } finally {
    addLoading.value = false
  }
}
</script>


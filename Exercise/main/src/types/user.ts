export type Gender = '남성' | '여성' | '기타'

export interface User {
  id: string
  userCode?: string  // 사용자 코드 (4자리 영문+숫자)
  name: string
  gender: Gender
  age: number
  height: number
  weight?: number
  faceUrl?: string
  chronicDiseases?: string[]
  position?: { lat: number; lng: number }
  guardianPhone?: string  // 보호자 연락처
}


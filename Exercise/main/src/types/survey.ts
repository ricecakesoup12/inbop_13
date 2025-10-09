export interface SurveyForm {
  // 1. 기본 정보
  name: string
  gender: string[]
  age: string
  height: string
  weight: string
  lifestyle: string[]
  
  // 2. 건강 상태
  diseases: string[]
  otherDisease: string
  surgery: string[]
  medication: string[]
  medicationName: string
  sleep: string[]
  
  // 3. 생활 습관
  smoking: string[]
  alcohol: string[]
  diet: string[]
  
  // 4. 운동 경험 및 체력
  exerciseFrequency: string[]
  preferredExercise: string[]
  otherExercise: string
  exerciseGoal: string[]
  otherGoal: string
  fitnessLevel: string
  
  // 5. 신체적 한계 및 특이사항
  painArea: string[]
  otherPainArea: string
  exerciseRestriction: string[]
  
  // 6. 심리/동기 요인
  exerciseReason: string[]
  otherReason: string
  exerciseConfidence: string[]
  exerciseEnvironment: string[]
}

export interface SurveyResult extends Partial<SurveyForm> {
  id: string
  userId: string
  createdAt: string
}


export interface VitalNow {
  hr: number
  spo2: number
  active: boolean
  ts: string
}

export interface DailyMetrics {
  date: string
  weight?: number
  avgHr?: number
  avgSpO2?: number
  activity?: number
}


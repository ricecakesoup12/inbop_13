export interface VitalNow {
  hr: number
  active: boolean
  ts: string
}

export interface DailyMetrics {
  date: string
  weight?: number
  avgHr?: number
  activity?: number
}


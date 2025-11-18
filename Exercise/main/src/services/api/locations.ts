import { http } from './http'

export interface LocationDto {
  userId: string
  latitude: number
  longitude: number
  timestamp: number
}

export interface LocationCreateDto {
  latitude: number
  longitude: number
  timestamp?: number
}

/**
 * 사용자 위치를 서버에 저장/업데이트
 */
export async function upsertLocation(userId: string, location: LocationCreateDto): Promise<void> {
  await http.post(`/api/locations/${userId}`, location)
}

/**
 * 모든 사용자의 최신 위치를 조회
 */
export async function getAllLocations(): Promise<LocationDto[]> {
  const { data } = await http.get('/api/locations')
  return data
}



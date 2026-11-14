export type Priority = 'CHEAPEST' | 'FASTEST' | 'GREENEST' | 'BALANCED'
export type TransportMode = 'CAR' | 'TRAIN' | 'BUS' | 'BIKE'

export interface ModeOption {
  mode: TransportMode
  label: string
  costPln: number
  durationMinutes: number
  co2Kg: number
  score: number
  rank: number
  recommended: boolean
  feasible: boolean
  note: string
}

export interface RouteMeta {
  distanceSource?: string
  weatherSource?: string
  carDurationMinutesFromOsrm?: number | null
}

export interface CompareResponse {
  origin: string
  destination: string
  distanceKm: number
  priority: Priority
  recommendedMode: TransportMode
  meta?: RouteMeta
  ranking: ModeOption[]
}

export interface Weather {
  city: string
  condition: string
  labelPl: string
  temperatureC: number
  bikeFriendly: boolean
}

export interface AgentAdviseResponse {
  recommendation: string
  reasoning: string
  recommendedMode: TransportMode
  detectedPriority: Priority
  parsedConstraints: Record<string, unknown>
  weather: Weather
  comparison: CompareResponse
}

export interface TripQuery {
  id: number
  origin: string
  destination: string
  distanceKm: number
  priority: Priority
  notes?: string
  resultSnapshotJson?: string
  createdAt: string
}

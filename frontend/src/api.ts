import type { AgentAdviseResponse, CompareResponse, Priority, TripQuery } from './types'

const API_BASE = 'http://localhost:8080/api'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  })

  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `HTTP ${response.status}`)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

export function compareTrip(body: {
  origin: string
  destination: string
  distanceKm?: number | null
  priority: Priority
  avoidParking?: boolean
  maxHours?: number | null
}) {
  return request<CompareResponse>('/compare', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function askAgent(message: string) {
  return request<AgentAdviseResponse>('/agent/advise', {
    method: 'POST',
    body: JSON.stringify({ message }),
  })
}

export function listTrips() {
  return request<TripQuery[]>('/trips')
}

export function createTrip(body: {
  origin: string
  destination: string
  distanceKm: number
  priority: Priority
  notes?: string
  resultSnapshotJson?: string
}) {
  return request<TripQuery>('/trips', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function updateTrip(
  id: number,
  body: {
    origin: string
    destination: string
    distanceKm: number
    priority: Priority
    notes?: string
    resultSnapshotJson?: string
  },
) {
  return request<TripQuery>(`/trips/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}

export function deleteTrip(id: number) {
  return request<void>(`/trips/${id}`, { method: 'DELETE' })
}

export interface CitySuggestion {
  name: string
  label: string
  country: string
  countryCode: string
  lat: number
  lon: number
}

export function searchCities(query: string, limit = 6) {
  const params = new URLSearchParams({ q: query, limit: String(limit) })
  return request<CitySuggestion[]>(`/cities/search?${params}`)
}

import type { Priority, TransportMode } from './types'

export function formatDuration(minutes: number): string {
  const total = Math.round(minutes)
  const h = Math.floor(total / 60)
  const m = total % 60
  if (h === 0) return `${m} min`
  return `${h} h ${m} min`
}

export function priorityLabel(priority: Priority): string {
  switch (priority) {
    case 'CHEAPEST':
      return 'najtaniej'
    case 'FASTEST':
      return 'najszybciej'
    case 'GREENEST':
      return 'najzieleniej'
    default:
      return 'zrównoważony'
  }
}

export function modeShort(mode: TransportMode): string {
  switch (mode) {
    case 'CAR':
      return 'Auto'
    case 'TRAIN':
      return 'PKP'
    case 'BUS':
      return 'Bus'
    case 'BIKE':
      return 'Bike'
  }
}

const LOCATIVE: Record<string, string> = {
  Katowice: 'Katowicach',
  Kraków: 'Krakowie',
  Warszawa: 'Warszawie',
  Wrocław: 'Wrocławiu',
  Poznań: 'Poznaniu',
  Gdańsk: 'Gdańsku',
  Gdynia: 'Gdyni',
  Łódź: 'Łodzi',
  Lublin: 'Lublinie',
  Rzeszów: 'Rzeszowie',
  Bydgoszcz: 'Bydgoszczy',
  Toruń: 'Toruniu',
  Szczecin: 'Szczecinie',
  Gliwice: 'Gliwicach',
}

export function locativeCity(city: string): string {
  return LOCATIVE[city] ?? city
}

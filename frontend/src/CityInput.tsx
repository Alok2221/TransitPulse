import { useEffect, useId, useState } from 'react'
import { searchCities, type CitySuggestion } from './api'

type Props = {
  label: string
  value: string
  onChange: (value: string) => void
  required?: boolean
}

export function CityInput({ label, value, onChange, required }: Props) {
  const listId = useId()
  const [suggestions, setSuggestions] = useState<CitySuggestion[]>([])

  useEffect(() => {
    if (value.trim().length < 2) {
      setSuggestions([])
      return
    }
    const handle = window.setTimeout(() => {
      void searchCities(value.trim())
        .then(setSuggestions)
        .catch(() => setSuggestions([]))
    }, 280)
    return () => window.clearTimeout(handle)
  }, [value])

  return (
    <label className="city-input">
      {label}
      <input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        list={listId}
        required={required}
        autoComplete="off"
        placeholder="np. Opole, Kielce, Berlin…"
      />
      <datalist id={listId}>
        {suggestions.map((item) => (
          <option key={`${item.name}-${item.lat}-${item.lon}`} value={item.name}>
            {item.label}
          </option>
        ))}
      </datalist>
    </label>
  )
}

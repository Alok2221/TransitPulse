import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { askAgent, compareTrip, createTrip, deleteTrip, listTrips, updateTrip } from './api'
import { CityInput } from './CityInput'
import { formatDuration, locativeCity, modeShort, priorityLabel } from './format'
import type { AgentAdviseResponse, CompareResponse, Priority, TripQuery } from './types'
import './App.css'

const PRIORITIES: { value: Priority; label: string }[] = [
  { value: 'BALANCED', label: 'Zrównoważony' },
  { value: 'CHEAPEST', label: 'Najtaniej' },
  { value: 'FASTEST', label: 'Najszybciej' },
  { value: 'GREENEST', label: 'Najzieleniej' },
]

export default function App() {
  const [origin, setOrigin] = useState('Katowice')
  const [destination, setDestination] = useState('Kraków')
  const [distanceKm, setDistanceKm] = useState('')
  const [priority, setPriority] = useState<Priority>('BALANCED')
  const [avoidParking, setAvoidParking] = useState(false)
  const [aiMessage, setAiMessage] = useState(
    'Jutro rano muszę być w Krakowie z Katowic, mam 2h, nie chcę płacić za parking',
  )
  const [comparison, setComparison] = useState<CompareResponse | null>(null)
  const [agent, setAgent] = useState<AgentAdviseResponse | null>(null)
  const [trips, setTrips] = useState<TripQuery[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [editingId, setEditingId] = useState<number | null>(null)

  useEffect(() => {
    void refreshTrips()
  }, [])

  async function refreshTrips() {
    try {
      setTrips(await listTrips())
    } catch {
      // API may be offline on first paint
    }
  }

  async function handleCompare(event: FormEvent) {
    event.preventDefault()
    setLoading(true)
    setError(null)
    setAgent(null)
    try {
      const result = await compareTrip({
        origin,
        destination,
        distanceKm: distanceKm ? Number(distanceKm) : null,
        priority,
        avoidParking,
      })
      setComparison(result)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Nie udało się porównać trasy')
    } finally {
      setLoading(false)
    }
  }

  async function handleAskAi(event: FormEvent) {
    event.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const result = await askAgent(aiMessage)
      setAgent(result)
      setComparison(result.comparison)
      setOrigin(result.comparison.origin)
      setDestination(result.comparison.destination)
      setPriority(result.detectedPriority)
      setDistanceKm(String(result.comparison.distanceKm))
      if (typeof result.parsedConstraints.avoidParking === 'boolean') {
        setAvoidParking(result.parsedConstraints.avoidParking)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Agent nie odpowiedział')
    } finally {
      setLoading(false)
    }
  }

  async function handleSave() {
    if (!comparison) return
    setLoading(true)
    setError(null)
    try {
      const payload = {
        origin: comparison.origin,
        destination: comparison.destination,
        distanceKm: comparison.distanceKm,
        priority: comparison.priority,
        notes: agent?.recommendation ?? `Porównanie: ${priorityLabel(comparison.priority)}`,
        resultSnapshotJson: JSON.stringify({
          recommendedMode: comparison.recommendedMode,
          ranking: comparison.ranking,
          agent: agent
            ? {
                reasoning: agent.reasoning,
                weather: agent.weather,
              }
            : null,
        }),
      }
      if (editingId != null) {
        await updateTrip(editingId, payload)
        setEditingId(null)
      } else {
        await createTrip(payload)
      }
      await refreshTrips()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Nie udało się zapisać')
    } finally {
      setLoading(false)
    }
  }

  async function handleDelete(id: number) {
    setLoading(true)
    setError(null)
    try {
      await deleteTrip(id)
      if (editingId === id) setEditingId(null)
      await refreshTrips()
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Nie udało się usunąć')
    } finally {
      setLoading(false)
    }
  }

  function loadTrip(trip: TripQuery) {
    setOrigin(trip.origin)
    setDestination(trip.destination)
    setDistanceKm(String(trip.distanceKm))
    setPriority(trip.priority)
    setEditingId(trip.id)
    if (trip.resultSnapshotJson) {
      try {
        const snap = JSON.parse(trip.resultSnapshotJson) as {
          ranking?: CompareResponse['ranking']
          recommendedMode?: CompareResponse['recommendedMode']
          agent?: { reasoning?: string; weather?: AgentAdviseResponse['weather'] }
        }
        if (snap.ranking) {
          setComparison({
            origin: trip.origin,
            destination: trip.destination,
            distanceKm: trip.distanceKm,
            priority: trip.priority,
            recommendedMode: snap.recommendedMode ?? snap.ranking[0]?.mode,
            ranking: snap.ranking,
          })
        }
        if (snap.agent?.reasoning) {
          setAgent({
            recommendation: trip.notes ?? '',
            reasoning: snap.agent.reasoning,
            recommendedMode: snap.recommendedMode ?? 'TRAIN',
            detectedPriority: trip.priority,
            parsedConstraints: {},
            weather: snap.agent.weather ?? {
              city: trip.destination,
              condition: 'clear',
              labelPl: 'brak danych',
              temperatureC: 0,
              bikeFriendly: true,
            },
            comparison: {
              origin: trip.origin,
              destination: trip.destination,
              distanceKm: trip.distanceKm,
              priority: trip.priority,
              recommendedMode: snap.recommendedMode ?? 'TRAIN',
              ranking: snap.ranking ?? [],
            },
          })
        } else {
          setAgent(null)
        }
      } catch {
        setAgent(null)
      }
    }
  }

  return (
    <div className="app">
      <header className="hero">
        <p className="hero-kicker">Decision tool · A → B</p>
        <h1 className="brand">TransitPulse</h1>
        <p className="hero-lead">
          Jak dojadę z punktu A do B — i ile mnie to kosztuje pieniędzy, czasu oraz CO₂?
        </p>
        <div className="hero-cta">
          <a href="#compare" className="btn btn-primary">
            Porównaj trasę
          </a>
          <a href="#ai" className="btn btn-ghost">
            Zapytaj AI
          </a>
        </div>
        <div className="hero-pulse" aria-hidden="true" />
      </header>

      <main>
        <section id="compare" className="panel reveal">
          <div className="section-head">
            <h2>Porównanie</h2>
            <p>Samochód, pociąg, autobus i rower — ranking pod Twój priorytet.</p>
          </div>

          <form className="form-grid" onSubmit={handleCompare}>
            <CityInput label="Skąd" value={origin} onChange={setOrigin} required />
            <CityInput label="Dokąd" value={destination} onChange={setDestination} required />
            <label>
              Dystans (km, opcjonalnie)
              <input
                type="number"
                min="1"
                step="0.1"
                placeholder="np. 80"
                value={distanceKm}
                onChange={(e) => setDistanceKm(e.target.value)}
              />
            </label>
            <label>
              Priorytet
              <select value={priority} onChange={(e) => setPriority(e.target.value as Priority)}>
                {PRIORITIES.map((item) => (
                  <option key={item.value} value={item.value}>
                    {item.label}
                  </option>
                ))}
              </select>
            </label>
            <label className="checkbox">
              <input
                type="checkbox"
                checked={avoidParking}
                onChange={(e) => setAvoidParking(e.target.checked)}
              />
              Nie chcę płacić za parking
            </label>
            <div className="form-actions">
              <button className="btn btn-primary" type="submit" disabled={loading}>
                {loading ? 'Liczą…' : 'Porównaj'}
              </button>
              {comparison && (
                <button className="btn btn-ghost" type="button" onClick={() => void handleSave()} disabled={loading}>
                  {editingId != null ? 'Zaktualizuj zapis' : 'Zapisz w historii'}
                </button>
              )}
            </div>
          </form>
        </section>

        <section id="ai" className="panel reveal delay">
          <div className="section-head">
            <h2>TransitPulse AI</h2>
            <p>Opisz plan podróży swoimi słowami — podpowiemy, czym jechać i dlaczego.</p>
          </div>
          <form className="ai-form" onSubmit={handleAskAi}>
            <textarea
              rows={4}
              value={aiMessage}
              onChange={(e) => setAiMessage(e.target.value)}
              placeholder="np. Jutro rano muszę być w Krakowie z Katowic…"
              required
            />
            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? 'Analizuję…' : 'Zapytaj agenta'}
            </button>
          </form>
          {agent && (
            <div className="agent-box">
              <p className="agent-rec">{agent.recommendation}</p>
              <p className="agent-reason">{agent.reasoning}</p>
              <p className="agent-meta">
                W {locativeCity(agent.weather.city)}: {agent.weather.labelPl}
                {agent.weather.bikeFriendly ? ' — rower ma sens' : ' — rower raczej odpada'}
                {agent.comparison.meta?.weatherSource === 'open-meteo' ? ' (dane pogodowe na żywo)' : ''}
              </p>
            </div>
          )}
        </section>

        {error && <p className="error">{error}</p>}

        {comparison && (
          <section className="results reveal">
            <div className="section-head">
              <h2>
                Ranking · {comparison.origin} → {comparison.destination}
              </h2>
              <p>
                {comparison.distanceKm} km · priorytet: {priorityLabel(comparison.priority)}
                {comparison.meta?.distanceSource
                  ? ` · dystans: ${comparison.meta.distanceSource}`
                  : ''}
                {comparison.meta?.weatherSource
                  ? ` · pogoda: ${comparison.meta.weatherSource}`
                  : ''}
              </p>
            </div>
            <div className="rank-list">
              {comparison.ranking.map((item, index) => (
                <article
                  key={item.mode}
                  className={`rank-item ${item.recommended ? 'is-top' : ''} ${item.feasible ? '' : 'is-weak'}`}
                  style={{ animationDelay: `${index * 80}ms` }}
                >
                  <div className="rank-left">
                    <span className="rank-num">#{item.rank}</span>
                    <span className={`mode-chip mode-${item.mode.toLowerCase()}`}>{modeShort(item.mode)}</span>
                    <div>
                      <h3>{item.label}</h3>
                      <p>{item.note}</p>
                    </div>
                  </div>
                  <div className="rank-metrics">
                    <div>
                      <span>Koszt</span>
                      <strong>{item.costPln.toFixed(2)} zł</strong>
                    </div>
                    <div>
                      <span>Czas</span>
                      <strong>{formatDuration(item.durationMinutes)}</strong>
                    </div>
                    <div>
                      <span>CO₂</span>
                      <strong>{item.co2Kg.toFixed(2)} kg</strong>
                    </div>
                    <div>
                      <span>Score</span>
                      <strong>{item.score.toFixed(0)}</strong>
                    </div>
                  </div>
                  {item.recommended && <span className="badge">Rekomendacja</span>}
                </article>
              ))}
            </div>
          </section>
        )}

        <section className="panel history reveal">
          <div className="section-head">
            <h2>Historia tras</h2>
            <p>CRUD zapisanych porównań w SQLite.</p>
          </div>
          {trips.length === 0 ? (
            <p className="muted">Brak zapisów — porównaj trasę i kliknij „Zapisz w historii”.</p>
          ) : (
            <ul className="trip-list">
              {trips.map((trip) => (
                <li key={trip.id}>
                  <div>
                    <strong>
                      {trip.origin} → {trip.destination}
                    </strong>
                    <span>
                      {trip.distanceKm} km · {priorityLabel(trip.priority)}
                    </span>
                  </div>
                  <div className="trip-actions">
                    <button type="button" className="btn btn-ghost" onClick={() => loadTrip(trip)}>
                      Wczytaj
                    </button>
                    <button type="button" className="btn btn-danger" onClick={() => void handleDelete(trip.id)}>
                      Usuń
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>

      <footer className="footer">
        <span>TransitPulse MVP</span>
        <span>API · localhost:8080</span>
      </footer>
    </div>
  )
}

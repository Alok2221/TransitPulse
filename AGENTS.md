# Instrukcje dla agentów (TransitPulse)

Ten plik opisuje, jak skonfigurowano narzędzia AI przy budowie MVP.

## Cursor rules

Reguły w [`.cursor/rules/`](.cursor/rules/):

- `git-workflow.mdc` — bez push/merge/force bez prośby użytkownika
- `konwencje-git.mdc` — gałęzie `feature/...`, commity po polsku bez `feat:`/`fix:`
- `transitpulse-stack.mdc` — podział BE/FE, CORS, SQLite, legalne API bez kluczy

Wzorce przeniesione z projektu **na-czas**, bez reguł Expo/EAS.

## Stack (skrót)

- `backend/` — Spring Boot 3 + SQLite
- `frontend/` — React + Vite + TypeScript
- Dane: Open-Meteo Geocoding/Weather, OSRM; fallback seed/haversine/mock
- Agent produktowy: lokalny parser NL, **bez** klucza LLM

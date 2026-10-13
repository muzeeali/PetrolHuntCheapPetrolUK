# Petrol Map: UK Fuel Prices

A fast, map-first UK petrol station discovery and fuel-price comparison mobile application built with Jetpack Compose, backed by a Cloudflare Worker API gateway and decoupled provider architecture.

---

## 1. Project Overview

**Petrol Map: UK Fuel Prices** helps UK drivers quickly find nearby petrol stations, compare fuel prices (E10, E5, Diesel), identify cheaper forecourts within a user-chosen radius (1–50 miles), and navigate seamlessly to their selected station using external navigation apps (Google Maps, Waze, or system default).

### Key Features
- **Map-First Dark UI:** Designed to Stitch specifications with custom OpenStreetMap dark-style maps and live price pill markers.
- **Color-Coded Station Markers:** Vibrant green for cheapest stations, amber for average prices, and red for expensive options.
- **Provider Decoupled Architecture:** Mobile app talks only to the Petrol Map API on Cloudflare Workers; provider logic (GOV.UK Fuel Finder & CMA Open Data Scheme) is completely normalized on the edge.
- **Dynamic Fuel & Radius Filtering:** Instant switching between E10, E5, and Diesel (B7), plus radius adjustments from 1 to 50 miles.
- **Accurate Distance Calculation:** Haversine formula calculation on both client and edge.
- **External Navigation Integration:** Direct routing intents for Google Maps, Waze, and System Default maps.
- **Offline & Stale Cache Fallback:** Cached stations are saved locally and presented with clear offline banners if the network is unavailable.
- **Zero Paid Services:** Utilizes free Cloudflare Worker tier and Firebase Remote Config (100% free) for remote provider agility without mobile app releases.
- **Attribution:** Transparent data attribution: *"Prices sourced via Fuel Finder & CMA Open Data Scheme"*.

---

## 2. Architecture

```text
                     ┌────────────────────────────────────────┐
                     │ Petrol Map Mobile App (Android/Compose)│
                     └───────────────────┬────────────────────┘
                                         │ HTTPS
                                         ▼
                     ┌────────────────────────────────────────┐
                     │    Cloudflare Worker API Gateway       │
                     │  - Validation & Rate Limiting          │
                     │  - Haversine Distance & Radius Filter  │
                     │  - Cloudflare Edge Cache (~20ms)       │
                     │  - GOV.UK OAuth 2.0 Provider Adapter   │
                     └───────────────┬────────────────────────┘
                                     │
                 ┌───────────────────┴────────────────────┐
                 ▼                                        ▼
    ┌─────────────────────────┐              ┌─────────────────────────┐
    │ Firebase Remote Config  │              │    GOV.UK Fuel Finder   │
    │  - Provider Name        │              │  - OAuth 2.0 Token Flow │
    │  - Base URL & Endpoints │              │  - Real Forecourt Prices│
    │  - Attribution & Cache  │              └─────────────────────────┘
    └─────────────────────────┘
```

---

## 3. Endpoints & Cloudflare Workers

### Production Worker
- **URL:** `https://petrol-hunt-api-production.petrol-hunt.workers.dev`
- **Health Check:** `GET /health`
- **Station Detail:** `GET /v1/station?slug=...`
- **Stations:** `GET /v1/stations?lat=51.5074&lng=-0.1278&fuel=E10&radius=5&sort=cheapest`

---

## 4. Mobile Setup & Development

### Prerequisites
- JDK 23 (or JDK 17+)
- Android Studio / Android SDK (API Level 24 to 37)
- Gradle 9.6.0 (bundled via `gradlew`)

### Build Commands
```bash
# Verify Gradle version and toolchain
./gradlew.bat --version

# Run Unit Tests
./gradlew.bat test

# Build Debug APK
./gradlew.bat assembleDebug

# Output APK Location
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 5. Firebase Remote Config Setup

The app uses Firebase Remote Config (100% free, no billing required) to allow swapping the backend fuel provider dynamically without updating the mobile app:

| Parameter Key | Default Value | Description |
|---|---|---|
| `fuel_provider_name` | `fuel_finder` | Active provider identifier |
| `fuel_provider_base_url` | `https://www.fuel-finder.service.gov.uk` | Upstream provider URL |
| `fuel_provider_endpoint` | `/api/v1/oauth/generate_access_token` | Upstream endpoint |
| `fuel_provider_attribution` | `Prices sourced via Fuel Finder & CMA Open Data Scheme` | Required attribution text |
| `fuel_provider_cache_minutes` | `15` | Edge cache duration |
| `fuel_provider_enabled` | `true` | Provider operational switch |
| `fuel_provider_version` | `2` | Configuration schema version |

---

## 6. Testing & Quality Assurance

- **Unit Tests:** verifies:
  - FuelType enum codes and display titles
  - Cheapest, Nearest, and Latest station sorting algorithms
  - Haversine distance formula accuracy
  - Navigation app package intents
- **API Tests:** Live edge requests against Cloudflare Worker proxy verified in real time.
- **Static Analysis & Build:** Clean Gradle assembly with zero compilation errors.

---

## 7. Legal & Disclaimer

Petrol Map is an independent automotive utility and is not affiliated with, operated by, or endorsed by the UK Government. Fuel prices are sourced via Fuel Finder & CMA Open Data Scheme.

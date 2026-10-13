# AGENT.md

# Petrol Hunt: Cheap Petrol UK

## 1. Mission

Build and ship a production-ready mobile application named:

**Petrol Hunt: Cheap Petrol UK**

The app helps UK drivers find nearby petrol stations, compare current fuel prices, identify cheaper fuel, and navigate to a selected station.

The application must be:

- Fast
- Minimal
- Map-first
- Reliable
- Production-ready
- Easy to maintain
- Provider-independent
- Designed for Android and iOS
- Built with a secure backend architecture
- Able to change fuel-data providers without requiring a mobile-app release

The primary objective is:

> **Open the app → find nearby fuel prices → compare stations → choose a station → get directions.**

Do not add unnecessary features.

---

# 2. FINAL PRODUCT DECISIONS

These decisions are finalized unless explicitly changed later.

## App name

**Petrol Hunt: Cheap Petrol UK**

Do not rename the app during implementation.

## App purpose

A UK fuel-price discovery and comparison utility.

The app is NOT:

- A government application
- An official UK Government application
- A petrol-station operator application
- A payment application
- A loyalty application
- A social network
- A fuel purchasing application

Never claim that Petrol Hunt is an official government service.

---

# 3. PRIMARY FUEL DATA PROVIDER

## Current provider

**CheckFuelPrices.co.uk**

The current provider integration should use the CheckFuelPrices station/widget data infrastructure.

Current station endpoint supplied for implementation:

```text
https://checkfuelprices.co.uk/api/widget/stations
```

Example:

```text
https://checkfuelprices.co.uk/api/widget/stations?lat=51.5074&lng=-0.1278&fuel=E10
```

The exact response schema must be inspected during implementation.

Do NOT assume field names.

Do NOT hard-code a guessed JSON schema.

Do NOT invent undocumented query parameters.

The provider adapter must first inspect and validate the actual response.

CheckFuelPrices currently documents:

- GPS coordinates
- Fuel type
- Radius
- Sorting
- Station limit
- Brand filtering
- UK postcode/city search

The documented widget radius is currently **1–50 miles**, with 5 miles as the default.

---

# 4. PROVIDER ABSTRACTION IS MANDATORY

Never couple the mobile UI directly to CheckFuelPrices.

Use this architecture:

```text
Mobile App
    ↓
Petrol Hunt API
    ↓
Cloudflare Worker
    ↓
Fuel Provider Adapter
    ↓
CheckFuelPrices
```

The mobile application must only communicate with the Petrol Hunt backend.

The app must NOT directly call:

```text
checkfuelprices.co.uk
```

---

# 5. PROVIDER ADAPTER

Create a provider abstraction.

Conceptually:

```text
FuelProvider
    └── CheckFuelPricesProvider
```

The provider interface should expose normalized operations such as:

```text
getNearbyStations()
getStationsByLocation()
```

The mobile application must never know:

- CheckFuelPrices field names
- CheckFuelPrices URL structure
- Provider-specific response fields
- Provider-specific sorting
- Provider-specific internal implementation

The provider adapter converts external provider data into the Petrol Hunt internal schema.

---

# 6. NORMALIZED STATION MODEL

The backend must normalize provider data into a stable schema.

Example:

```json
{
  "id": "provider-station-id",
  "name": "Example Petrol Station",
  "brand": "Example",
  "latitude": 51.5074,
  "longitude": -0.1278,
  "address": {
    "line1": "Example Street",
    "town": "London",
    "postcode": "SW1A 1AA"
  },
  "prices": {
    "E10": 139.9,
    "E5": 145.9,
    "B7": 148.9,
    "SDV": null
  },
  "distanceMiles": 1.2,
  "updatedAt": "2026-10-07T12:30:00Z",
  "source": "checkfuelprices"
}
```

This is an example internal schema.

Do not assume the provider returns these exact fields.

The adapter must map the actual provider response into this structure.

---

# 7. FUEL TYPES

Primary supported fuel types:

```text
E10
E5
B7
SDV
```

UI should present human-readable labels:

```text
E10 Petrol
E5 Petrol
Diesel
Premium Diesel
```

Only show a fuel type when valid data is available.

If the provider does not return a particular fuel type, do not fabricate a price.

---

# 8. PRICE RULES

Prices must always be numeric internally.

Example:

```text
139.9
```

Display:

```text
139.9p
```

Never store:

```text
"£1.399"
```

as the primary price value.

Never fabricate prices.

If a station has no price for the selected fuel:

```text
Price unavailable
```

Do not include unavailable prices in cheapest-price calculations.

---

# 9. USER LOCATION

The mobile application obtains the user's current location using the device location permission.

Location is used to:

1. Find nearby fuel stations.
2. Center the map.
3. Calculate station distance.
4. Query the backend.
5. Sort stations by distance where required.

Do not request unnecessary permissions.

Required permission:

```text
Location
```

Do not request:

- Contacts
- Camera
- Microphone
- SMS
- Call logs
- Photos
- Bluetooth

unless a future feature explicitly requires them.

---

# 10. RADIUS

The user must be able to select a search radius.

Recommended options:

```text
1 mile
3 miles
5 miles
10 miles
15 miles
25 miles
50 miles
```

Default:

```text
5 miles
```

The backend must validate the radius.

Allowed range:

```text
1–50 miles
```

Do not allow arbitrary huge radius values.

Example:

```text
radius=10
```

means:

> Find stations within approximately 10 miles of the requested coordinates.

---

# 11. IMPORTANT RADIUS IMPLEMENTATION RULE

Do not blindly assume that the provider's underlying station endpoint accepts:

```text
radius=10
```

because the documented widget supports radius but the underlying station endpoint may have different implementation details.

During implementation:

1. Inspect the actual provider response.
2. Test the supplied station endpoint.
3. Determine whether provider-side radius filtering works.
4. If provider-side radius filtering is unavailable or unreliable, retrieve the permitted station set and calculate distance server-side.
5. Never expose undocumented provider behavior to the mobile app.

The app must always receive a correctly radius-filtered result set.

---

# 12. DISTANCE CALCULATION

When server-side filtering is required, use the Haversine formula.

```text
a =
sin²(Δφ / 2)
+
cos(φ1) × cos(φ2) × sin²(Δλ / 2)

c =
2 × atan2(√a, √(1-a))

distance =
R × c
```

For miles:

```text
R = 3958.8
```

Display:

```text
1.2 mi
```

Use one decimal place.

---

# 13. SORTING

Supported sorting:

```text
Cheapest
Nearest
```

Default:

```text
Cheapest
```

Cheapest means:

> Lowest valid price for the currently selected fuel type.

Nearest means:

> Lowest calculated distance from the user's selected location.

Never sort unavailable prices as zero.

---

# 14. CLOUDflare ARCHITECTURE

Cloudflare Worker is the backend gateway.

Architecture:

```text
                    ┌──────────────────────┐
                    │   Petrol Hunt App    │
                    └──────────┬───────────┘
                               │
                               │ HTTPS
                               ▼
                    ┌──────────────────────┐
                    │   Cloudflare Worker  │
                    │                      │
                    │ Authentication       │
                    │ Validation           │
                    │ Rate limiting        │
                    │ Caching              │
                    │ Provider adapter     │
                    │ Normalization         │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ CheckFuelPrices      │
                    │ Provider             │
                    └──────────┬───────────┘
                               │
                               ▼
                    UK station/fuel data
```

Cloudflare Workers supports encrypted secrets for sensitive values such as API keys/tokens. Do not put secrets in plaintext Worker variables.

---

# 15. CLOUDflare MCP

The implementation agent MUST use the available **Cloudflare MCP** for Cloudflare-related infrastructure operations where supported.

Use Cloudflare MCP to:

- Inspect the Cloudflare account
- Create/manage the Worker
- Deploy the Worker
- Configure Worker environments
- Configure routes/custom domains where required
- Configure KV/cache infrastructure if needed
- Configure secrets
- Inspect deployment status
- Verify production Worker behavior

Do not manually invent Cloudflare resource IDs.

Do not assume an existing Worker exists.

First inspect the available Cloudflare resources through MCP.

If the required MCP operation is unavailable, use the official Cloudflare-supported mechanism instead.

---

# 16. CLOUDFLARE ENVIRONMENTS

Maintain separate environments:

```text
development
staging
production
```

Recommended naming:

```text
petrol-hunt-api-development
petrol-hunt-api-staging
petrol-hunt-api-production
```

Do not test production configuration directly during development.

Cloudflare recommends deliberate environment separation for Workers.

---

# 17. CLOUDflare SECRETS

Never hard-code secrets.

Never place secrets inside:

```text
Flutter source code
React Native source code
JavaScript bundle
Git repository
Firebase client-readable configuration
DESIGN.md
AGENT.md
```

If CheckFuelPrices ever requires an API key or token, store it as a Cloudflare Worker secret.

Example conceptual secret:

```text
CHECKFUELPRICES_API_KEY
```

Do not assume the provider currently requires one.

Cloudflare's Worker secret mechanism is specifically intended for API keys and authentication tokens.

---

# 18. FIREBASE PURPOSE

Firebase is NOT the primary fuel-price database.

Do NOT store thousands of station prices in Firebase.

Do NOT use Firebase as the live station-price cache.

Firebase is used for:

> **Remote provider configuration and application configuration that may need to change without publishing a new mobile-app version.**

---

# 19. FIREBASE MCP

The implementation agent MUST use the available **Firebase MCP** for Firebase-related setup/configuration where supported.

Use Firebase MCP to:

- Inspect Firebase projects
- Create/configure the required Firebase resources
- Create the provider configuration document
- Update provider configuration when changing providers
- Verify configuration
- Maintain environment-specific configuration where supported

Do not invent Firebase project IDs.

First inspect available Firebase projects/resources through MCP.

If Firebase MCP does not expose a required operation, use the official Firebase tooling/API.

---

# 20. FIREBASE PROVIDER CONFIGURATION

Create a configuration structure similar to:

```text
apiProviders/
    active
```

Example:

```json
{
  "provider": "checkfuelprices",
  "enabled": true,
  "baseUrl": "https://checkfuelprices.co.uk",
  "stationsPath": "/api/widget/stations",
  "version": 1,
  "attribution": "Prices from CheckFuelPrices.co.uk",
  "cacheMinutes": 30
}
```

This is configuration, not fuel-price storage.

---

# 21. WHY FIREBASE CONFIGURATION EXISTS

The goal is to allow this:

```text
Today:

CheckFuelPrices
        ↓
Cloudflare Worker
        ↓
Petrol Hunt
```

If the provider changes later:

```text
Tomorrow:

NewProvider
        ↓
Cloudflare Worker
        ↓
Petrol Hunt
```

without requiring a mobile application update.

The provider adapter remains responsible for translating the provider's response.

---

# 22. IMPORTANT FIREBASE SECURITY RULE

Do not put sensitive provider credentials into publicly readable Firebase documents.

Firebase configuration may contain:

```text
Provider name
Provider URL
Endpoint path
Cache duration
Attribution text
Feature flags
```

It must NOT contain:

```text
API secrets
Private tokens
Passwords
Cloudflare credentials
Firebase service-account credentials
```

Sensitive credentials belong in Cloudflare secrets.

---

# 23. CONFIGURATION FLOW

Preferred production flow:

```text
Firebase
   ↓
Provider configuration
   ↓
Cloudflare Worker
   ↓
Provider adapter
   ↓
CheckFuelPrices
```

The mobile application should NOT directly fetch Firebase configuration to determine the fuel provider.

The Worker controls the provider.

This prevents exposing infrastructure details to the client.

---

# 24. CONFIGURATION CACHING

Do not query Firebase on every user request.

The Worker should cache provider configuration.

Example:

```text
Firebase configuration
        ↓
Cloudflare Worker
        ↓
short/appropriate configuration cache
        ↓
API requests
```

If Firebase is temporarily unavailable, continue using the last valid provider configuration where safely possible.

If no valid configuration exists:

```text
Provider unavailable
```

Return a controlled error.

Never crash the application.

---

# 25. CLOUDflare CACHE STRATEGY

Fuel prices are not second-by-second financial data.

Use approximately:

```text
30-minute cache
```

as the initial target, consistent with the provider's documented update cadence.

Cache key should include relevant query inputs.

Conceptually:

```text
stations:
lat rounded
lng rounded
fuel
radius
```

Example:

```text
stations:51.507,-0.128:E10:5
```

Do not create unlimited unique cache keys from excessive coordinate precision.

Round coordinates appropriately to improve cache reuse while maintaining useful location accuracy.

---

# 26. STALE CACHE FALLBACK

If CheckFuelPrices fails:

```text
Live provider request
        ↓
Failure
        ↓
Last successful cached response
        ↓
Return cached response
```

Response metadata should indicate:

```json
{
  "dataStatus": "cached",
  "fetchedAt": "...",
  "source": "checkfuelprices"
}
```

The mobile app can then display:

```text
Offline · Saved prices
```

or:

```text
Prices may be slightly out of date
```

Do not pretend cached data is live.

---

# 27. NETWORK TIMEOUT

The Worker must use a reasonable upstream timeout.

If the provider does not respond:

1. Stop waiting after timeout.
2. Attempt cached response.
3. If no cached response exists, return controlled error.
4. Do not leave the mobile request hanging indefinitely.

---

# 28. RATE LIMITING

Protect the Worker.

Rate-limit abusive requests.

Validate:

```text
latitude
longitude
fuel
radius
sort
```

Reject:

- Invalid coordinates
- Impossible coordinates
- Negative radius
- Radius > 50
- Unknown fuel type
- Unknown sort mode
- Excessively frequent requests

---

# 29. BACKEND API

Expose a clean Petrol Hunt API.

Example:

```text
GET /v1/stations
```

Parameters:

```text
lat
lng
fuel
radius
sort
```

Example:

```text
GET /v1/stations?lat=51.5074&lng=-0.1278&fuel=E10&radius=5&sort=price_low
```

The mobile app only knows this API.

It does not know the CheckFuelPrices endpoint.

---

# 30. BACKEND RESPONSE

Example:

```json
{
  "success": true,
  "source": "checkfuelprices",
  "dataStatus": "live",
  "fetchedAt": "2026-10-07T12:30:00Z",
  "location": {
    "latitude": 51.5074,
    "longitude": -0.1278
  },
  "fuel": "E10",
  "radiusMiles": 5,
  "stations": []
}
```

The actual station objects must use the normalized internal model.

---

# 31. ERROR RESPONSE

Use predictable errors.

Example:

```json
{
  "success": false,
  "error": {
    "code": "PROVIDER_UNAVAILABLE",
    "message": "Fuel prices are temporarily unavailable."
  }
}
```

Possible error codes:

```text
INVALID_LOCATION
INVALID_FUEL
INVALID_RADIUS
PROVIDER_UNAVAILABLE
NO_RESULTS
RATE_LIMITED
INTERNAL_ERROR
```

Do not expose provider stack traces or internal credentials.

---

# 32. MOBILE APPLICATION ARCHITECTURE

Preferred architecture:

```text
Presentation
    ↓
State Management
    ↓
Repository
    ↓
Petrol Hunt API Client
    ↓
Cloudflare Worker
```

Never:

```text
UI
 ↓
CheckFuelPrices
```

---

# 33. LOCAL STORAGE

No custom database is required for the mobile application.

Use lightweight local storage for:

```text
selectedFuel
selectedRadius
sortMode
navigationApp
recentSearches
lastLocation
cachedStations
cacheTimestamp
```

Use an appropriate key-value storage mechanism.

Do not create a local relational database unless a future feature actually requires it.

---

# 34. OFFLINE BEHAVIOR

If the user previously loaded stations:

```text
No internet
    ↓
Show cached stations
    ↓
Display "Offline · Saved prices"
```

If there is no cache:

```text
No saved prices

Connect to the internet to find fuel prices.
```

The app must remain usable for:

- Viewing cached stations
- Opening cached station details
- Viewing cached prices
- Opening navigation where possible

---

# 35. MAP

The map is the primary UI.

Use an OpenStreetMap-compatible map solution.

The map should:

- Center on user location
- Display station markers
- Display price on markers
- Support pan/zoom
- Support clustering when needed
- Update results after location/search changes
- Avoid excessive network calls

---

# 36. MARKER DESIGN

Price markers:

```text
139.9p
```

Use price-based visual hierarchy.

Cheap:

```text
Green
```

Average:

```text
Amber
```

Expensive:

```text
Red
```

Do not use excessive colors.

The cheapest station should be visually obvious but not oversized.

---

# 37. MAIN MAP SCREEN

Main screen structure:

```text
┌──────────────────────────────┐
│ Search town or postcode      │
├──────────────────────────────┤
│ E10  E5  Diesel             │
├──────────────────────────────┤
│                              │
│                              │
│            MAP               │
│                              │
│        139.9p                │
│                141.2p        │
│   138.9p                     │
│                              │
│                       ◎      │
├──────────────────────────────┤
│ Cheapest nearby              │
│                              │
│ Station name                 │
│ 1.2 mi       139.9p          │
│                              │
│ Station name                 │
│ 2.1 mi       140.4p          │
└──────────────────────────────┘
```

Map should occupy most of the screen.

---

# 38. SEARCH

Support:

- UK postcode
- Town
- City
- Area

Examples:

```text
SW1A 1AA
Manchester
Birmingham
Leeds
```

Recent searches:

- Store locally
- Maximum 5
- Allow clear/remove

Search must not require an account.

---

# 39. FUEL SELECTOR

Primary choices:

```text
E10
E5
Diesel
```

Additional fuel options:

```text
B7
SDV
```

only when supported by the normalized data.

Changing fuel must refresh:

- Map prices
- Station list
- Cheapest sorting
- Station details

---

# 40. FILTERS

Filters:

```text
Fuel
Radius
Brand (optional)
```

Do not add unnecessary filters.

---

# 41. SORTING

Sorting UI:

```text
Sort by

○ Cheapest
○ Nearest
```

Default:

```text
Cheapest
```

---

# 42. STATION DETAILS

Station details screen must show:

```text
Station name

139.9p
E10

1.2 mi

Address

Fuel prices:
E10     139.9p
E5      145.9p
Diesel  148.9p

[ Get Directions ]
```

Optional:

```text
View on map
```

---

# 43. DIRECTIONS

Petrol Hunt does NOT provide internal turn-by-turn navigation.

Use external navigation applications.

Supported:

```text
Google Maps
Apple Maps
Waze
```

Preference:

```text
System default
Google Maps
Apple Maps
Waze
```

If selected navigation application is unavailable, fall back to another available navigation application.

---

# 44. SETTINGS

Settings must contain:

## Preferences

```text
Fuel type
Distance
Sort order
```

## Navigation

```text
Preferred navigation app
```

## Data

```text
Last update
Refresh prices
Clear saved data
```

## Privacy

```text
Location
Privacy Policy
```

## About

```text
About Petrol Hunt
Fuel data
Terms
Open-source licenses
Version
```

---

# 45. LOCATION DENIED

If location permission is denied:

Show:

```text
Location is off

You can search for a town or postcode instead.

[ Search Location ]
```

Never block the entire app solely because location permission was denied.

---

# 46. LOADING STATE

Do not replace the entire map with a loading screen.

Use:

```text
Finding fuel...
```

or a small loading indicator.

Existing cached data should remain visible while refreshing.

---

# 47. REFRESHING STATE

When refreshing:

```text
Existing stations remain visible
        +
small refresh indicator
```

Do not blank the screen.

---

# 48. EMPTY STATE

If no stations are found:

```text
No fuel nearby

Try a wider radius or search another location.
```

Buttons:

```text
[ Increase radius ]
[ Search another location ]
```

---

# 49. ERROR STATE

If network/provider fails:

```text
Couldn't update prices

Try again in a moment.
```

Button:

```text
[ Try Again ]
```

If cached results exist, show cached results instead.

---

# 50. PROVIDER ATTRIBUTION

Because CheckFuelPrices requires attribution for its service usage, include an appropriate visible attribution such as:

```text
Prices from CheckFuelPrices.co.uk
```

Do not hide required attribution.

Place it in an appropriate location such as:

- Data information screen
- Station results information area
- About/Fuel Data screen

Confirm final attribution requirements against the provider's current terms before production release.

---

# 51. GOVERNMENT DATA CLAIM

Do not say:

```text
Official Government Petrol Hunt
```

Do not say:

```text
Government Fuel App
```

Do not imply ownership by the UK Government.

If explaining data provenance, use factual wording such as:

```text
Fuel prices are provided through our fuel-data provider.
```

If mentioning government-origin data, only do so where verified by the provider's current documentation.

---

# 52. API PROVIDER SWITCHING

The most important backend requirement is provider independence.

Current:

```text
Firebase config
provider = checkfuelprices

        ↓

Cloudflare

        ↓

CheckFuelPricesProvider
```

Future:

```text
Firebase config
provider = anotherprovider

        ↓

Cloudflare

        ↓

AnotherFuelProvider
```

The mobile application remains unchanged.

---

# 53. PROVIDER ADAPTER INTERFACE

Conceptually:

```typescript
interface FuelProvider {
    getStations(params): Promise<NormalizedStation[]>;
}
```

Current:

```typescript
class CheckFuelPricesProvider implements FuelProvider {
    getStations(params) {
        // CheckFuelPrices integration
    }
}
```

Future:

```typescript
class AnotherProvider implements FuelProvider {
    getStations(params) {
        // New provider
    }
}
```

Do not leak provider-specific logic outside the adapter.

---

# 54. FIREBASE CONFIG VERSIONING

Provider configuration should include:

```text
version
updatedAt
enabled
provider
baseUrl
stationsPath
cacheMinutes
attribution
```

Example:

```json
{
  "version": 1,
  "provider": "checkfuelprices",
  "enabled": true,
  "baseUrl": "https://checkfuelprices.co.uk",
  "stationsPath": "/api/widget/stations",
  "cacheMinutes": 30,
  "attribution": "Prices from CheckFuelPrices.co.uk"
}
```

The implementation agent must not put actual secrets in this document or repository.

---

# 55. SECURITY

Never expose:

- Cloudflare API tokens
- Firebase service credentials
- Provider private credentials
- Admin credentials

in:

- Mobile application
- Git
- Public Firebase documents
- Source control
- Client logs
- API responses

Cloudflare secrets should be used for sensitive Worker credentials.

---

# 56. LOGGING

Backend logs should contain useful operational information:

```text
request ID
provider
fuel
radius
response status
response duration
cache hit/miss
error code
```

Never log:

```text
API secrets
tokens
authorization headers
private credentials
```

---

# 57. OBSERVABILITY

Track:

```text
Provider success rate
Provider failure rate
Average response time
Cache hit rate
Cache miss rate
No-result rate
API error rate
Rate-limit events
```

Do not collect unnecessary personal information.

---

# 58. PRIVACY

Location is sensitive.

Use location only for:

- Finding nearby stations
- Calculating distance
- Centering the map

Do not permanently store precise location on the server unless explicitly required.

Prefer:

```text
Device location
      ↓
API request
      ↓
Station results
```

Do not create a location history system.

Recent searches can remain local.

---

# 59. NO USER ACCOUNTS

The initial application does not require:

```text
Login
Signup
Email
Password
Profile
Social account
```

Do not add authentication unless explicitly requested later.

---

# 60. NO FAVORITES IN V1

Do not implement:

```text
User accounts
Cloud favorites
Cloud history
Saved vehicles
```

unless explicitly requested.

Local settings are sufficient for V1.

---

# 61. PERFORMANCE

Target:

- Fast initial map rendering
- Minimal API calls
- 30-minute backend cache
- Smooth map interaction
- Efficient markers
- Marker clustering where necessary
- Debounced map movement
- No unnecessary Firebase requests
- No unnecessary provider requests

When the map moves rapidly, debounce requests approximately:

```text
500–800ms
```

---

# 62. MAP QUERY OPTIMIZATION

Do not send a provider request for every tiny map movement.

Only refresh station data when:

- User location changes meaningfully
- Search location changes
- Radius changes
- Fuel changes
- User explicitly refreshes
- Map movement exceeds an appropriate threshold

---

# 63. CACHE KEY

Use normalized cache keys.

Example:

```text
stations:v1:
51.507:
-0.128:
E10:
5
```

Do not use unlimited decimal precision.

---

# 64. CACHE INVALIDATION

Invalidate cached results when:

```text
fuel changes
radius changes
location changes significantly
provider changes
provider configuration version changes
```

Respect the provider's intended update cadence.

---

# 65. API VALIDATION

Before sending upstream:

```text
latitude >= -90
latitude <= 90

longitude >= -180
longitude <= 180

radius >= 1
radius <= 50

fuel ∈ E10,E5,B7,SDV
```

Reject invalid requests.

---

# 66. PROVIDER RESPONSE VALIDATION

Treat external provider data as untrusted input.

Validate:

```text
station ID
name
latitude
longitude
prices
address
```

Discard malformed station records.

Never crash because one station contains malformed data.

---

# 67. DUPLICATE STATIONS

If the provider returns duplicates:

1. Prefer stable provider ID.
2. If no ID exists, derive a safe temporary key from normalized station identity/location.
3. Deduplicate before returning data to the app.

---

# 68. INVALID PRICES

Reject:

```text
negative prices
zero prices
non-numeric prices
impossible values
```

Do not silently convert malformed values to fake prices.

---

# 69. DATA FRESHNESS

Backend response should expose:

```text
fetchedAt
dataStatus
```

Possible:

```text
live
cached
stale
```

Never label old fallback data as live.

---

# 70. TESTING

Before production:

## Provider tests

Test:

```text
E10
E5
Diesel/B7
different coordinates
different radius
empty results
malformed station
provider timeout
provider HTTP error
```

## Backend tests

Test:

```text
invalid coordinates
invalid radius
invalid fuel
rate limiting
cache hit
cache miss
stale fallback
provider unavailable
```

## Mobile tests

Test:

```text
location allowed
location denied
GPS unavailable
internet available
internet unavailable
cached results
no cache
fuel switching
radius switching
sorting
station details
directions
search
```

---

# 71. ACCEPTANCE TEST — CORE FLOW

The implementation is not complete until this flow works:

```text
Launch Petrol Hunt
        ↓
Allow location
        ↓
Map opens
        ↓
Nearby stations appear
        ↓
Fuel prices appear
        ↓
Select E10
        ↓
Select 10 mile radius
        ↓
Results update
        ↓
Select Cheapest
        ↓
Cheapest stations appear first
        ↓
Tap station
        ↓
Station details open
        ↓
Tap Get Directions
        ↓
External navigation opens
```

---

# 72. ACCEPTANCE TEST — PROVIDER SWITCH

Verify that changing:

```text
Firebase provider configuration
```

does not require:

```text
Mobile application rebuild
```

where the new provider implements the same normalized adapter contract.

---

# 73. ACCEPTANCE TEST — PROVIDER FAILURE

Simulate:

```text
CheckFuelPrices unavailable
```

Expected:

```text
Cloudflare
    ↓
cached result
    ↓
Petrol Hunt
```

If no cache exists:

```text
Petrol prices temporarily unavailable.
```

The app must remain stable.

---

# 74. ACCEPTANCE TEST — API SECURITY

Verify:

```text
Mobile app cannot see provider secrets
Mobile app cannot directly access private provider credentials
Cloudflare secrets are not exposed
Firebase configuration contains no secrets
Logs contain no credentials
```

---

# 75. ACCEPTANCE TEST — RADIUS

Test:

```text
1 mile
3 miles
5 miles
10 miles
15 miles
25 miles
50 miles
```

Verify stations outside the selected radius are not displayed.

---

# 76. ACCEPTANCE TEST — LOCATION

Test:

```text
London
Manchester
Birmingham
Leeds
Liverpool
```

Verify that the backend correctly handles different UK locations.

---

# 77. UI QUALITY BAR

The app must feel like a premium modern automotive utility.

Design characteristics:

```text
Dark
Minimal
Clean
Fast
Map-first
Compact
Professional
```

Avoid:

```text
Huge headings
Excessive cards
Excessive text
Unnecessary illustrations
Social-feed UI
Government-portal appearance
Cluttered dashboards
```

---

# 78. BRAND COLORS

Use the finalized Petrol Hunt palette.

```text
Background:       #0F1115
Surface:          #171A20
Surface Elevated: #1E222A
Border:           #292E38

Primary Text:     #FFFFFF
Secondary Text:   #9AA3B2
Muted Text:       #6F7887

Brand Green:      #35E879
Dark Green:       #20B85E

Cheap:            #35E879
Average:          #FFC857
Expensive:        #FF5C5C
```

---

# 79. NAVIGATION

Bottom navigation:

```text
Map
Settings
```

No unnecessary navigation tabs.

---

# 80. MOBILE SCREENS

Required screens:

1. Splash
2. Location permission
3. Main Map
4. Search
5. Search Results
6. Fuel Selector
7. Filter
8. Sort
9. Station Details
10. Directions selection
11. Loading
12. Refreshing
13. Empty
14. Offline
15. Network Error
16. Location Denied
17. Settings
18. Fuel Preference
19. Distance Preference
20. Sort Preference
21. Navigation Preference
22. Data Settings
23. Clear Data Confirmation
24. Privacy
25. Fuel Data
26. About
27. Terms
28. Open Source Licenses

---

# 81. DO NOT IMPLEMENT

Do not add:

```text
Social features
Chat
Accounts
Payments
Subscriptions
Fuel purchasing
Loyalty cards
Rewards
News feed
Advertising SDKs
Unnecessary analytics
Government login
Government branding
```

unless explicitly requested later.

---

# 82. API DOCUMENTATION

Document the internal API.

At minimum:

```text
GET /v1/stations
```

Document:

```text
Parameters
Validation
Response
Errors
Caching
Provider
Attribution
```

Do not document internal secrets.

---

# 83. README REQUIREMENTS

The repository README should explain:

```text
Project
Architecture
Mobile setup
Cloudflare setup
Firebase setup
Environment configuration
Development
Staging
Production
Testing
Deployment
Provider configuration
Troubleshooting
```

---

# 84. ENVIRONMENT CONFIGURATION

Example conceptual configuration:

```text
APP_ENV=production

FUEL_PROVIDER=checkfuelprices

FUEL_PROVIDER_BASE_URL=https://checkfuelprices.co.uk

CACHE_MINUTES=30
```

Sensitive credentials must NOT be placed in normal configuration variables.

Use Cloudflare secrets for sensitive values.

---

# 85. FIREBASE CONFIGURATION DOCUMENT

Use an environment-aware configuration structure.

Conceptually:

```text
config/
    development/
        fuelProvider
    staging/
        fuelProvider
    production/
        fuelProvider
```

The exact Firebase structure may be adapted to the Firebase MCP/tooling capabilities.

The important requirement is:

> Provider configuration must be remotely changeable and environment-aware.

---

# 86. CLOUDflare MCP IMPLEMENTATION ORDER

The agent should execute infrastructure in this order:

```text
1. Inspect Cloudflare account
2. Inspect existing Workers
3. Inspect available domains
4. Create/select Petrol Hunt Worker
5. Create development environment
6. Create staging environment
7. Create production environment
8. Configure secrets
9. Implement provider adapter
10. Implement cache
11. Implement API routes
12. Deploy staging
13. Test staging
14. Deploy production
15. Verify production
```

Do not skip staging.

---

# 87. FIREBASE MCP IMPLEMENTATION ORDER

```text
1. Inspect Firebase projects
2. Select/create Petrol Hunt Firebase project
3. Create configuration structure
4. Add CheckFuelPrices provider configuration
5. Add environment separation
6. Configure secure access
7. Verify configuration
8. Connect Cloudflare Worker
9. Test configuration retrieval
10. Test provider switching
```

---

# 88. MCP SAFETY

The agent must never:

- Delete an existing production Firebase project without explicit approval.
- Delete an existing production Cloudflare Worker without explicit approval.
- Overwrite unrelated projects.
- Rotate production secrets without understanding dependencies.
- Modify unrelated DNS records.
- Expose secrets in logs.

Before destructive operations:

```text
STOP
VERIFY RESOURCE
VERIFY ENVIRONMENT
ASK FOR CONFIRMATION
```

---

# 89. IMPORTANT PROVIDER RULE

The implementation agent must not assume:

```text
CheckFuelPrices = permanent provider
```

It is:

```text
Current provider
```

The architecture must make replacement easy.

---

# 90. FUTURE PROVIDER EXAMPLE

If a future provider becomes necessary:

```text
Firebase:

provider = newprovider

        ↓

Cloudflare

        ↓

NewProviderAdapter

        ↓

NormalizedStation

        ↓

Petrol Hunt
```

The mobile application should continue working.

---

# 91. PRODUCTION DEFINITION

The application is considered production-ready only when:

- Mobile app works on Android
- iOS architecture is supported if using cross-platform framework
- Cloudflare Worker is deployed
- Firebase configuration is active
- CheckFuelPrices integration works
- Radius filtering works
- Fuel filtering works
- Cheapest sorting works
- Nearest sorting works
- Station details work
- Navigation works
- Offline cache works
- Provider failure fallback works
- Attribution is present
- No secrets are exposed
- No fake prices are used
- No provider-specific code exists in UI
- Staging tests pass
- Production tests pass

---

# 92. FINAL ARCHITECTURE

The final architecture is:

```text
                         PETROL HUNT
                              │
                              ▼
                     ┌────────────────┐
                     │ Mobile App     │
                     │ Android / iOS  │
                     └───────┬────────┘
                             │
                             │ HTTPS
                             ▼
                  ┌──────────────────────┐
                  │ Cloudflare Worker    │
                  │                      │
                  │ API Gateway          │
                  │ Validation           │
                  │ Rate Limit           │
                  │ Cache                │
                  │ Provider Adapter     │
                  │ Error Handling       │
                  └───────┬──────────────┘
                          │
              ┌───────────┴────────────┐
              │                        │
              ▼                        ▼
      ┌─────────────────┐      ┌─────────────────┐
      │ Firebase        │      │ CheckFuelPrices │
      │ Provider Config │      │ Fuel Data       │
      └─────────────────┘      └─────────────────┘
              │                        │
              │                        │
              └──────────┬─────────────┘
                         ▼
                Normalized Station Data
                         │
                         ▼
                  Petrol Hunt Mobile App
                         │
             ┌───────────┴────────────┐
             ▼                        ▼
          Map View              Station Details
                                      │
                                      ▼
                              External Navigation
```

---

# 93. SINGLE SOURCE OF TRUTH

The responsibilities are finalized as follows:

| Responsibility | System |
|---|---|
| Mobile UI | Petrol Hunt app |
| User GPS | Device |
| Fuel provider | CheckFuelPrices |
| Provider integration | Cloudflare Worker |
| API gateway | Cloudflare Worker |
| Caching | Cloudflare |
| Rate limiting | Cloudflare |
| Provider configuration | Firebase |
| Provider switching | Firebase + Cloudflare adapter |
| Sensitive secrets | Cloudflare Secrets |
| Station normalization | Cloudflare Worker |
| Distance calculation | Backend/app as appropriate |
| Map | OpenStreetMap-compatible provider |
| Navigation | Google Maps / Apple Maps / Waze |
| User preferences | Local mobile storage |

---

# 94. FINAL GOAL

The final product must accomplish this reliably:

```text
USER OPENS PETROL HUNT
        ↓
APP GETS LOCATION
        ↓
APP REQUESTS PETROL HUNT API
        ↓
CLOUDFLARE CHECKS CACHE
        ↓
IF NEEDED:
CLOUDFLARE REQUESTS CHECKFUELPRICES
        ↓
PROVIDER DATA IS VALIDATED
        ↓
PROVIDER DATA IS NORMALIZED
        ↓
RESULTS ARE FILTERED BY LOCATION/RADIUS
        ↓
RESULTS ARE SORTED
        ↓
CLOUDFLARE RETURNS CLEAN JSON
        ↓
PETROL HUNT SHOWS:
MAP + PRICES + DISTANCES
        ↓
USER SELECTS STATION
        ↓
STATION DETAILS
        ↓
GET DIRECTIONS
        ↓
EXTERNAL NAVIGATION APP
```

## Core principle

**The mobile app should never care where fuel prices come from.**

The mobile app only knows:

```text
Petrol Hunt API
```

Cloudflare controls:

```text
Provider
Caching
Validation
Rate limiting
Normalization
Fallback
```

Firebase controls:

```text
Changeable provider configuration
```

CheckFuelPrices currently provides:

```text
UK fuel-price data
```

This separation is mandatory for the production architecture.
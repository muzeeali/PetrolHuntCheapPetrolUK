# DESIGN.md — Petrol Hunt: Cheap Petrol UK

## 1. Project Overview

**App name:** Petrol Hunt: Cheap Petrol UK  
**Platform:** Android and iOS  
**Design tool:** Google Stitch  
**App category:** UK petrol-price finder  
**Design direction:** Minimal, premium, dark, map-first automotive utility

### Product Goal

Help UK drivers quickly find nearby petrol stations, compare fuel prices, identify cheaper fuel, and navigate to a selected station.

The primary user journey is:

**Open app → Find nearby stations → Compare prices → Select station → Get directions**

The interface must be easy to understand within seconds. Prioritize the map, fuel prices, and nearby station results over decorative content.

### Core Features

- Find nearby UK petrol stations.
- Compare E10, E5, and diesel prices.
- Search by UK postcode, town, city, or area.
- Adjust search radius from 1 to 50 miles.
- Sort stations by cheapest or nearest.
- View station details and available fuel prices.
- Open directions in Google Maps, Apple Maps, or Waze.
- Display cached results when offline.
- Configure preferences through Settings.
- Show fuel-data attribution.

---

## 2. Design Principles

1. **Map first:** The map is the main screen and occupies most of the available space.
2. **Prices first:** Fuel prices must be the most visually prominent station information.
3. **Minimal text:** Use concise labels and short messages.
4. **Green with purpose:** Reserve the brand green primarily for actions, selection, and cheaper prices.
5. **Compact components:** Avoid oversized cards, excessive padding, and unnecessarily large headings.
6. **Consistent screens:** Every screen must use the same colors, typography, spacing, and component styles.
7. **Native-feeling controls:** Use familiar mobile interactions, bottom sheets, segmented controls, and compact settings rows.
8. **Honest information:** Never show fabricated prices or imply that stale data is live.
9. **Accessible design:** Maintain readable contrast, clear selection states, and touch targets of approximately 44–48 dp or larger.
10. **Original branding:** Create original Petrol Hunt branding. Do not copy competitor logos, screenshots, proprietary icons, or layouts.

---

## 3. Brand Identity

### App Name

**Petrol Hunt: Cheap Petrol UK**

Use the full name in store-facing branding and About information. Within the application, use the shorter name **Petrol Hunt** wherever space is limited.

### Brand Personality

- Smart
- Fast
- Trustworthy
- Practical
- Modern
- Automotive
- Price-conscious

Avoid government-portal styling, finance-dashboard styling, excessive gradients, cartoon illustrations, and busy interfaces.

### Logo Usage

Use the approved Petrol Hunt logo asset consistently.

- App icon: centered logo with suitable padding.
- Splash screen: logo centered on the dark background.
- About screen: small logo and full app name.
- Do not redraw, distort, recolor, or crop the supplied logo without authorization.
- If the logo has a transparent background, preserve its transparency wherever appropriate.

---

## 4. Color System

Use the following exact color tokens throughout the app.

| Token | Hex | Usage |
|---|---|---|
| Background | `#0F1115` | Main screen background |
| Surface | `#171A20` | Cards and bottom navigation |
| Elevated Surface | `#1E222A` | Sheets, menus, dialogs |
| Border | `#292E38` | Subtle separators and outlines |
| Primary Text | `#FFFFFF` | Titles and important values |
| Secondary Text | `#9AA3B2` | Addresses and supporting information |
| Muted Text | `#6F7887` | Secondary metadata |
| Brand Green | `#35E879` | Primary actions and selected states |
| Dark Green | `#20B85E` | Pressed states and darker green accents |
| Cheap Price | `#35E879` | Comparatively cheap prices |
| Average Price | `#FFC857` | Mid-range prices |
| Expensive Price | `#FF5C5C` | Comparatively expensive prices |
| Error | `#FF5C5C` | Errors and destructive warnings |
| Success | `#35E879` | Successful operations |

### Color Rules

- The overall interface must remain predominantly dark.
- Use green selectively rather than coloring every component.
- Use amber and red only where price comparison or status requires them.
- Never communicate price status using color alone; retain the numerical price.
- Use subtle borders rather than bright outlines.
- Keep map markers readable against the dark map.
- Do not use bright green as a large full-screen background.

---

## 5. Typography

**Preferred font:** Inter  
**Fallback:** Platform system sans-serif

| Element | Size | Weight |
|---|---:|---|
| Main price | 28–32 sp | Bold |
| Station price | 20–24 sp | Bold |
| Screen title | 22 sp | Bold |
| Section title | 16 sp | Semibold |
| Station name | 15–16 sp | Semibold |
| Body text | 14 sp | Regular |
| Secondary text | 12–13 sp | Regular |
| Button text | 14 sp | Semibold |
| Small metadata | 11–12 sp | Regular |

### Typography Rules

- Use short, readable headings.
- Prices must be easier to scan than station metadata.
- Avoid all-uppercase headings except short fuel labels where appropriate.
- Use consistent numeric formatting.
- Display prices in pence, such as `139.9p`, when the provider supplies a valid price in pence per litre.
- Never convert or infer price units without verifying the provider's data format.

---

## 6. Spacing and Layout

Use a consistent 4-point spacing system.

```text
4 dp   — tiny spacing
8 dp   — compact spacing
12 dp  — standard internal spacing
16 dp  — default screen padding
20 dp  — component separation
24 dp  — section spacing
32 dp  — major separation
```

### Layout Rules

- Default horizontal padding: 16 dp.
- Compact control height: approximately 40–44 dp.
- Primary button height: approximately 48–52 dp.
- Standard card radius: 12 dp.
- Large bottom-sheet radius: 16–20 dp.
- Small input radius: 10–12 dp.
- Pill controls: fully rounded.
- Use 1 dp subtle borders where necessary.
- Respect safe areas, device cutouts, and system navigation insets.

Do not add large empty gaps to screens that should remain compact.

---

## 7. Navigation Structure

Use a minimal bottom navigation bar with two destinations:

1. **Map**
2. **Settings**

The Map destination is the default screen.

The Search, Filter, Sort, Fuel Selector, and Station Details experiences should be presented as contextual screens, dialogs, or bottom sheets rather than additional permanent navigation tabs.

Do not add History, Profile, Favorites, News, Rewards, or other tabs to the initial version.

---

## 8. Screen 1 — Splash Screen

### Purpose

Introduce the brand while the app initializes.

### Layout

- Full-screen background: `#0F1115`.
- Center the approved Petrol Hunt logo.
- Show the app name below the logo.
- Optional short tagline: **Find cheaper fuel.**
- Small loading indicator near the bottom if initialization takes time.

### Rules

- No onboarding carousel.
- No decorative illustrations.
- No unnecessary animation.
- Keep the splash screen brief.
- Transition directly to the map or the location-permission experience.

---

## 9. Screen 2 — First-Launch Location Permission

### Purpose

Explain why location access improves nearby station discovery.

### Layout

- Small location-pin illustration or simple line icon.
- Title: **Find fuel near you**
- Description: “Use your location to find nearby stations and compare prices.”
- Primary button: **Use my location**
- Secondary action: **Search manually**

### Behavior

- Request device location only after the user taps the primary action.
- If permission is denied, allow manual postcode or town search.
- Do not block the app if location permission is unavailable.
- Do not imply that the app needs continuous background location tracking.

---

## 10. Screen 3 — Main Map Screen

### This is the most important screen.

The map should occupy approximately 70–80% of the usable screen area when the station sheet is collapsed.

### Layout

**Top area**

- Search bar with search icon.
- Placeholder: `Search town or postcode`
- Compact filter button.
- Optional refresh indicator.

**Fuel selector**

Show compact horizontal chips:

- E10
- E5
- Diesel

Additional fuel grades should appear only when supported by the data provider and the app's normalized data model.

**Map**

- Dark OpenStreetMap-compatible map style.
- Nearby station markers displaying prices.
- Current-location indicator.
- Floating recenter-location button.
- Marker clustering at lower zoom levels if required.

**Bottom sheet**

- Drag handle.
- Heading: `Cheapest nearby`
- Compact result count or selected radius.
- Scrollable station rows.

**Bottom navigation**

- Map
- Settings

### Visual Hierarchy

1. Fuel prices on the map.
2. Selected fuel.
3. Search and filters.
4. Station list.
5. Secondary metadata.

### Map Behavior

- Initial map center: user location when permission is available.
- Otherwise, show a manual location-search prompt.
- Tapping a price marker highlights its station and updates the bottom sheet.
- Tapping a station row opens Station Details.
- Panning the map must not trigger excessive network requests.
- Refresh station results after a meaningful map movement, location change, fuel change, or radius change.
- Keep existing results visible while refreshing.

### Price Marker Design

Use compact rounded price pills, for example:

`139.9p`

Marker colors:

- Green: relatively cheap.
- Amber: mid-range.
- Red: relatively expensive.

The comparison baseline must be derived from valid available results. Do not assign a price category using fabricated market averages.

---

## 11. Screen 4 — Expanded Map Bottom Sheet

### Purpose

Allow users to compare nearby stations without leaving the map.

### Layout

- Rounded top corners.
- Drag handle.
- Heading: `Cheapest nearby`
- Small result count.
- Compact sort control.
- Scrollable station list.
- Map remains visible above the sheet when practical.

### States

**Collapsed:** A few station rows or a compact preview.

**Expanded:** A larger scrollable list.

**Dragging:** The sheet moves smoothly and does not interfere with map controls.

### Station Row

Each row must contain:

- Station name.
- Short address or area.
- Distance.
- Selected fuel price.
- Optional small cheapest indicator.

Example:

```text
Station name
High Street, London · 1.2 mi

139.9p
E10
```

Keep each row compact. Avoid large logos, multiple badges, or unnecessary descriptive paragraphs.

---

## 12. Screen 5 — Search

### Purpose

Find stations around a user-entered location.

### Layout

- Title: `Search location`
- Search field: `Postcode, town or city`
- Clear button.
- Recent searches section when available.
- Search suggestions/results.

### Supported Input

- UK postcodes.
- Towns.
- Cities.
- Areas.

Examples:

- SW1A 1AA
- Manchester
- Birmingham
- Leeds

### Behavior

- Show recent searches only if they exist.
- Store a maximum of five recent searches locally.
- Allow clearing recent searches.
- Selecting a location returns to the map centered on that location.
- Refresh station results using the selected location.

### Empty State

`Search for a town or postcode to find fuel nearby.`

---

## 13. Screen 6 — Search Results

This screen is used when location suggestions or resolved place results need a separate selection step.

### Layout

- Back button.
- Title: `Choose location`
- List of matching places.
- Place name and concise location subtitle.

### Behavior

- Tapping a result centers the map on that location.
- Do not show fake stations while the location is being resolved.
- If no match exists, show: `Location not found. Try another search.`

---

## 14. Screen 7 — Fuel Selector

### Purpose

Choose the fuel grade for station comparison.

### Layout

Use compact selectable rows or chips.

Primary options:

- E10 Petrol
- E5 Petrol
- Diesel

Additional options may include B7 or SDV only if the provider's data and fuel-grade mapping support them.

### Selected State

- Green border or green-tinted surface.
- Check icon.
- Clear label.

### Behavior

- Persist the selection locally.
- Refresh map markers and station rows after a change.
- Recalculate cheapest sorting for the selected grade.
- Never display a price for a fuel grade the station does not offer.

---

## 15. Screen 8 — Filter Bottom Sheet

### Purpose

Adjust the radius and any supported station filters.

### Layout

- Title: `Filters`
- Radius section.
- Optional brand filter, only if real provider data supports it.
- Primary button: `Apply filters`
- Secondary action: `Reset`

### Radius Options

Display compact chips or a single-choice list:

- 1 mi
- 3 mi
- 5 mi
- 10 mi
- 15 mi
- 25 mi
- 50 mi

Default: **5 miles**

### Behavior

- Only allow radius values from 1 to 50 miles.
- Apply the selected radius to the backend request.
- Ensure returned stations are actually filtered within the selected radius.
- Do not assume the provider's widget radius parameter is automatically supported by its underlying station endpoint.
- If the provider endpoint does not reliably enforce radius, filter results using verified station coordinates and a correct distance calculation.
- Show the active radius on the map or bottom sheet.

---

## 16. Screen 9 — Sort Bottom Sheet

### Purpose

Choose how stations are ordered.

### Layout

Title: `Sort by`

Single-choice options:

- Cheapest
- Nearest

### Defaults

Cheapest.

### Behavior

**Cheapest:** Sort by the selected fuel's valid price, lowest first.

**Nearest:** Sort by calculated distance from the current or manually selected location.

Stations without a valid selected-fuel price must not be treated as zero-price stations.

Persist the preference locally.

---

## 17. Screen 10 — Station Details

### Purpose

Show available station information and make navigation easy.

### Layout

**Header**

- Back button.
- Station name.
- Optional brand label if supplied by the provider.

**Main price**

- Large selected-fuel price.
- Fuel-grade label.
- Distance from the active location.
- Data freshness indicator when available.

**Address**

- Full available station address.
- Compact location icon.

**Fuel prices**

List valid prices available for this station:

```text
E10 Petrol       139.9p
E5 Petrol        145.9p
Diesel           148.9p
```

These are illustrative values only. Real UI must use actual provider data.

**Primary action**

`Get directions`

**Secondary action**

`View on map`

### Behavior

- Do not invent missing prices, station facilities, opening hours, or addresses.
- If a price is unavailable, display `Price unavailable` where appropriate.
- Make Get Directions the most prominent action.
- If the data provider exposes a last-updated timestamp, display it accurately.
- If the timestamp is missing, do not invent one.

---

## 18. Screen 11 — Get Directions

### Purpose

Open the selected station in an external navigation app.

### Layout

Title: `Get directions`

Options:

- System default
- Google Maps
- Apple Maps
- Waze

Use a compact native-feeling selection sheet.

### Behavior

- Use the station's verified coordinates or address.
- Open the selected navigation app when available.
- Fall back gracefully if the preferred app is not installed.
- Persist the preferred navigation app locally.
- Do not implement internal turn-by-turn navigation.

---

## 19. Screen 12 — Settings

### Purpose

Expose user preferences and app information without clutter.

### Layout

- Title: `Settings`
- Grouped settings sections.
- Compact list rows.
- Small leading icons.
- Clear secondary values.
- Subtle dividers.

### Section A — Preferences

- Fuel type — default E10.
- Search radius — default 5 miles.
- Sort order — default Cheapest.

### Section B — Navigation

- Preferred navigation app — default System default.

### Section C — Data

- Last successful update, when available.
- Refresh prices.
- Clear saved data.

### Section D — Privacy

- Location permission information.
- Privacy Policy.

### Section E — About

- About Petrol Hunt.
- Fuel data and attribution.
- Terms.
- Open-source licenses.
- App version.

### Behavior

- Preferences update immediately or after explicit confirmation, depending on the control.
- Settings changes must persist locally where applicable.
- Do not add login, user profiles, cloud favorites, or unnecessary settings.

---

## 20. Screen 13 — Fuel Preference

### Layout

- Title: `Default fuel`
- E10 Petrol
- E5 Petrol
- Diesel

Show a clear selected indicator.

### Behavior

- Save the selection locally.
- Refresh station results when returning to the map.
- Include additional grades only when supported.

---

## 21. Screen 14 — Distance Preference

### Layout

- Title: `Search radius`
- Options from 1 to 50 miles.
- Use the same radius options as the map filter.
- Selected value clearly indicated.

### Behavior

- Save the selection locally.
- Update map results.
- Do not exceed the supported 50-mile limit.

---

## 22. Screen 15 — Sort Preference

### Layout

- Title: `Default sorting`
- Cheapest
- Nearest

### Behavior

Save the selected option and use it when displaying results.

---

## 23. Screen 16 — Navigation Preference

### Layout

- Title: `Navigation app`
- System default
- Google Maps
- Apple Maps
- Waze

### Behavior

Save the choice locally. If the selected app is unavailable, provide a fallback.

---

## 24. Screen 17 — Data Settings

### Layout

- Title: `Fuel data`
- Last successful update, if available.
- `Refresh prices` button.
- `Clear saved data` action.
- Provider attribution.

### Behavior

- Refresh prices using the Petrol Hunt backend.
- Keep current results visible during refresh.
- Clearly distinguish live results from cached results.
- Clearing saved data requires confirmation.
- Do not expose backend configuration, Firebase project details, or provider credentials.

---

## 25. Screen 18 — Clear Saved Data Confirmation

### Layout

Title: `Clear saved data?`

Message:

`This removes saved station results and local search history.`

Actions:

- `Clear data`
- `Cancel`

### Behavior

- Remove only the relevant local cached results and recent searches.
- Preserve unrelated app preferences unless the user chooses a full reset.
- Return to Settings after completion.

---

## 26. Screen 19 — Loading State

### Purpose

Indicate that nearby station data is being loaded.

### Layout

- Small spinner or skeleton rows.
- Short message: `Finding fuel...`

### Behavior

- Do not display fake stations or prices.
- Keep the map shell visible.
- If cached results exist, display them while refreshing.
- Avoid blocking the entire app with a full-screen loader.

---

## 27. Screen 20 — Refreshing State

### Layout

- Existing map and results remain visible.
- Small spinner near the results heading or refresh control.
- Optional concise label: `Updating prices...`

### Behavior

Do not clear the current results simply because a refresh has started.

---

## 28. Screen 21 — Empty Results

### Layout

- Simple location or map-pin icon.
- Title: `No fuel nearby`
- Message: `Try a wider radius or search another location.`
- Primary action: `Increase radius`
- Secondary action: `Search location`

### Behavior

- Keep the screen minimal.
- Preserve the selected fuel.
- Do not populate the list with fabricated stations.

---

## 29. Screen 22 — Offline With Cached Results

### Layout

- Existing map and cached station list.
- Small neutral banner: `Offline · Saved prices`
- Optional cached-data age, only if accurately known.

### Behavior

- Continue showing saved stations.
- Make it clear that prices may be out of date.
- Allow station details and navigation using available cached data.
- Retry data retrieval when connectivity returns or the user refreshes.

---

## 30. Screen 23 — Offline Without Cached Results

### Layout

- Small connection icon.
- Title: `No saved prices`
- Message: `Connect to the internet to find fuel prices.`
- Button: `Try again`
- Manual location search remains accessible.

### Behavior

Do not imply that the app can retrieve new live prices without a working data connection.

---

## 31. Screen 24 — Network or Provider Error

### Layout

- Small warning icon.
- Title: `Couldn't update prices`
- Message: `Try again in a moment.`
- Button: `Try again`

### Behavior

- Show cached results if available.
- Avoid raw server errors and technical stack traces.
- Do not claim prices are live if the response came from a stale fallback.
- Do not clear the current map on a failed refresh.

---

## 32. Screen 25 — Location Denied

### Layout

- Location icon.
- Title: `Location is off`
- Message: `Search a town or postcode to find fuel prices.`
- Primary action: `Search location`
- Optional action: `Open settings`

### Behavior

The app must remain usable without location permission.

---

## 33. Screen 26 — About Petrol Hunt

### Layout

- Approved Petrol Hunt logo.
- App name: `Petrol Hunt: Cheap Petrol UK`
- Short description: `Find nearby fuel prices and compare stations.`
- App version.
- Fuel-data information link.
- Privacy Policy.
- Terms.
- Open-source licenses.

### Rules

Do not claim the app is owned, operated, or endorsed by the UK Government.

---

## 34. Screen 27 — Fuel Data and Attribution

### Purpose

Explain the source of the fuel-price information.

### Layout

Title: `Fuel data`

Display the required attribution prominently enough to be visible:

**Prices from CheckFuelPrices.co.uk**

Include a link to the provider's website or relevant data-information page.

Optionally show the last successful refresh time when accurate.

### Rules

- Follow the provider's current attribution requirements.
- Do not suggest the app is the provider.
- Do not claim all prices are live if results are cached.
- Only describe the upstream data source when verified by current provider documentation.

---

## 35. Screen 28 — Privacy

### Layout

Title: `Privacy`

Use concise sections:

- Location use.
- Local preferences and search history.
- Fuel-price requests.
- Data caching.
- Privacy Policy link.

### Rules

Explain actual implementation behavior, not planned or hypothetical functionality.

Do not claim that no data is transmitted if location coordinates are sent to the backend to retrieve nearby stations.

---

## 36. Screen 29 — Terms

### Layout

Title: `Terms`

Provide access to the application's terms of use and relevant provider disclaimers.

Do not present Petrol Hunt as an official government service.

---

## 37. Screen 30 — Open-Source Licenses

### Layout

Title: `Open-source licenses`

- List the actual libraries used.
- Use platform-appropriate license presentation.
- Keep the page readable and functional.

Do not create fake library names or license details.

---

## 38. Interaction and Component States

Every reusable component must have the appropriate states.

### Buttons

- Default.
- Pressed.
- Disabled.
- Loading, where appropriate.

### Fuel Chips

- Selected.
- Unselected.
- Disabled when unavailable.

### Station Rows

- Default.
- Pressed.
- Selected, where applicable.

### Inputs

- Empty.
- Focused.
- Filled.
- Clearable.
- Validation error.

### Network States

- Loading.
- Refreshing.
- Live results.
- Cached results.
- Empty.
- Error.

Maintain consistent spacing and state styling across screens.

---

## 39. Data and UI Integrity

The design must support the real application data model.

- Never generate fake prices for production.
- Never invent station names or addresses.
- Do not assume every station offers every fuel grade.
- Do not show a price without a valid source value.
- Distinguish unavailable prices from zero.
- Distinguish cached data from live data.
- Use actual station coordinates for navigation.
- Show only filters supported by the backend.
- Do not display a successful refresh state until the refresh succeeds.

Stitch should use illustrative placeholders only when generating static mockups, and clearly treat them as design examples rather than live data.

---

## 40. Backend Integration Expectations

The mobile app uses the Petrol Hunt API, backed by a Cloudflare Worker.

The Worker:

- Retrieves provider configuration from Firebase.
- Integrates with the current CheckFuelPrices station endpoint.
- Validates requests.
- Normalizes station data.
- Applies or verifies radius filtering.
- Supports fuel filtering and sorting.
- Caches responses.
- Returns cached data when appropriate.
- Reports whether data is live or cached.

The UI must not directly depend on CheckFuelPrices-specific JSON fields.

The provider URL must not be hard-coded across multiple UI components.

---

## 41. Responsive Design

Design for:

- Compact Android phones.
- Standard Android phones.
- Large Android phones.
- iPhones of different sizes.
- Portrait orientation as the primary experience.

### Responsive Rules

- The map expands to available space.
- The bottom sheet adapts to device height.
- Station rows remain readable on narrow displays.
- Search controls must not overlap system status areas.
- Long station names should truncate gracefully.
- Prices must remain visible.
- Bottom navigation must respect system insets.

Avoid requiring landscape orientation for any core workflow.

---

## 42. Accessibility

- Maintain strong text contrast against dark surfaces.
- Provide meaningful labels for icon-only buttons.
- Keep touch targets around 44–48 dp or larger where practical.
- Do not use color alone to indicate selected fuel or price status.
- Respect system text scaling without clipping critical content.
- Ensure bottom sheets and dialogs can be dismissed accessibly.
- Provide clear keyboard focus and input behavior.
- Use appropriate accessibility labels for map markers.

---

## 43. Animation

Use restrained animations only where they improve clarity.

Allowed:

- Smooth bottom-sheet expansion.
- Subtle marker selection.
- Small button feedback.
- Gentle screen transitions.
- Loading indicators.

Avoid:

- Long splash animations.
- Excessive bouncing.
- Decorative particle effects.
- Heavy parallax.
- Animation that delays access to fuel prices.

---

## 44. Explicitly Excluded Features

Do not design or implement these in the initial release:

- User registration or login.
- User profiles.
- Social feeds.
- Messaging.
- Fuel purchases.
- Loyalty schemes.
- Rewards.
- Subscription screens.
- Unrequested advertising.
- Vehicle management.
- Internal turn-by-turn navigation.
- History tab.
- Favorites tab.
- Government login.
- Unnecessary onboarding slides.

These are outside the finalized V1 scope.

---

## 45. Required Stitch Deliverables

Generate a consistent UI design for all required screens and important interaction states.

The deliverables must include:

1. Main Map screen.
2. Expanded and collapsed station bottom sheets.
3. Search screen.
4. Search results.
5. Fuel selector.
6. Filter sheet.
7. Sort sheet.
8. Station Details.
9. Get Directions selection.
10. First-launch location permission.
11. Settings.
12. Fuel Preference.
13. Distance Preference.
14. Sort Preference.
15. Navigation Preference.
16. Data Settings.
17. Clear Saved Data confirmation.
18. Loading state.
19. Refreshing state.
20. Empty results.
21. Offline with cached results.
22. Offline without cached results.
23. Network/provider error.
24. Location denied.
25. About Petrol Hunt.
26. Fuel Data and attribution.
27. Privacy.
28. Terms.
29. Open-source licenses.

Use reusable components and consistent design tokens.

Do not create unrelated screens merely to make the project appear more complete.

---

## 46. Final Design Acceptance Criteria

The design is approved only when:

- Petrol Hunt branding is consistent.
- The Map screen is the primary experience.
- Fuel prices are immediately visible.
- The interface is dark, compact, and readable.
- E10, E5, and Diesel are easy to select.
- The radius control includes 1–50 mile options.
- Cheapest and Nearest sorting are available.
- Station details provide a clear Get Directions action.
- Settings includes all required preferences and data/privacy information.
- Loading, empty, offline, and error states are designed.
- Fuel-data attribution is visible.
- No screen claims the app is government-operated.
- No unnecessary navigation tabs are included.
- No fabricated live data is presented.
- All screens use the same spacing, typography, colors, and component styles.

## Final Instruction to Stitch

Create a polished, production-oriented mobile UI for **Petrol Hunt: Cheap Petrol UK**.

Prioritize the map, price comparison, station selection, and directions flow. Use the exact color system, concise text, compact components, and screen requirements in this document.

Generate a coherent app rather than disconnected screen concepts. Reuse components and design tokens, preserve the finalized product scope, and do not introduce new features without authorization.
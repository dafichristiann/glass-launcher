# Minimalist Launcher — Design Spec (M0–M2)

**Date:** 2026-02-14
**Status:** Approved (design), pending written-spec review
**Scope of this cycle:** M0–M2 (project setup, HOME registration, home pager + dock + drawer, outline icons, three widgets)
**Platform:** Android 8.0+ (minSdk 26), targetSdk 35
**Language/UI:** Kotlin, Jetpack Compose
**Package name:** `com.dafichristian.launcher`

---

## 1. Intent and success criteria

This launcher is a **personal daily driver** — the user installs it on their own phone, sets it as
default home, and uses it every day. Success is that it feels calm and fast and that its built-in
widgets (date, day progress, calendar) are things the user actually relies on.

Primary success criteria:

- Installs and runs as default home on the user's device without crashes.
- Home page 1 matches the approved design and uses base color `#9CD4DB`.
- The app drawer lists every installed app and launches it on tap.
- The three widgets show correct, live data.
- Emulator-based instrumental tests pass.

Secondary (nice-to-have, not blockers): performance targets from the source PRD (cold start < 1 s,
60 fps scroll, < 120 MB idle) and multi-OEM robustness.

### Non-goals (this cycle)

- Quick Notes and Daily Tasks (M3, Room).
- Drag-and-drop reordering and long-press system shortcuts (M4).
- Settings screen wiring (M4/M5). The screen is drawn in the Pencil design for completeness only.
- Dark-mode toggle (deferred). Dark **tokens** are defined now, light is the default.
- Third-party widgets via AppWidgetHost (explicitly excluded from v1 entirely).
- Play Store preparation (not required for a personal driver).
- Reimplementing wallpaper picker, system settings, lock screen, gestures, or system search —
  these defer to stock Android.

---

## 2. Confirmed decisions

| # | Decision | Value |
|---|----------|-------|
| D1 | min/target SDK | minSdk 26, targetSdk 35 |
| D2 | First cycle scope | M0–M2 |
| D3 | Day Progress widget | 24 dots (one per hour) + percent text, minute-level updates |
| D4 | Dark mode | Tokens defined now; toggle deferred to a later cycle |
| D5 | Build approach | Typed slot model (Approach A) plus a shared Clock/TickerFlow |
| D6 | Artifact order | Written spec first, then Pencil artboards, then implementation plan |

---

## 3. Architecture

Single Gradle module `:app`. Multi-module overhead is not justified at this size; splitting is a
later refactor if build times become a problem. Single `LauncherActivity`, MVVM, unidirectional
data flow.

```
app/src/main/java/com/dafichristian/launcher/
 ├─ LauncherActivity.kt          # HOME intent, singleTask, Compose host
 ├─ LauncherApp.kt               # Application; DI entry
 ├─ core/
 │   ├─ time/Clock.kt            # Clock + TickerFlow (minute-aligned, screen-on aware)
 │   └─ time/DayProgress.kt      # pure calc: fraction of day elapsed + hour index
 ├─ data/
 │   ├─ apps/AppRepository.kt    # LauncherApps + Callback -> Flow<List<AppEntry>>
 │   ├─ apps/LauncherAppsSource.kt # thin interface over LauncherApps (mockable)
 │   ├─ apps/AppEntry.kt         # packageName, label, componentName, user, icon
 │   ├─ icons/IconProvider.kt    # outline mapping + monochrome fallback
 │   ├─ icons/OutlineIconMap.kt  # packageName -> painter (curated map)
 │   ├─ layout/LayoutRepository.kt   # slot map persisted in DataStore (Flow)
 │   ├─ layout/Slot.kt           # sealed: Widget | App | Empty
 │   ├─ layout/HomeLayout.kt     # List<Page>; serialization + default layout
 │   └─ prefs/SettingsRepository.kt  # baseColor, iconMode, gridCols, theme
 ├─ domain/                      # thin use cases; pure, unit-testable
 │   ├─ ObserveHomeLayout.kt
 │   ├─ ObserveAppList.kt
 │   └─ ResolveIcon.kt
 ├─ ui/
 │   ├─ theme/                  # Color, Type, Shape (dual light/dark tokens, light active)
 │   ├─ home/                   # HomeScreen, HomePager, Dock, PageIndicator
 │   ├─ widgets/                # DateCard, DayProgressCard, CalendarCard, CardFrame
 │   ├─ drawer/                 # AppDrawer, SearchBar
 │   ├─ icon/                   # AppIcon composable (outline vs original)
 │   └─ onboarding/             # short intro + "set as default"
 └─ di/                         # dependency injection modules
```

### Key interfaces (the seams that matter)

- `AppRepository.apps: Flow<List<AppEntry>>` — drawer and grid both read this. Install/uninstall
  events arrive via a `LauncherApps.Callback` into the same flow. Nothing else touches `LauncherApps`.
- `LayoutRepository.layout: Flow<HomeLayout>`, where `HomeLayout = List<Page>` and
  `Page = List<Slot>`. Home, dock, and page indicator all render from this single source. M4
  drag-and-drop mutates this flow; the UI re-renders.
- `Clock` — injectable; exposes `ticks(): Flow<Instant>` aligned to the next minute plus an
  immediate emission, and pauses while the display is off. Time-based cards depend on `Clock`;
  tests inject a fake.
- `IconProvider.iconFor(app: AppEntry, mode: IconMode)` — the single place outline-vs-original
  logic lives.

### Why this shape

Each unit has one clear purpose and knows nothing it does not need:

- `AppRepository` knows nothing about UI.
- `LayoutRepository` knows nothing about packages beyond their names.
- `Clock` knows nothing about widgets.

Icon rendering can change without touching the pager. M3's Notes/Tasks cards are added by adding
`Slot` kinds and composables only.

---

## 4. Data flow and state

### App list

`AppRepository` holds a `MutableStateFlow<List<AppEntry>>`. On start it queries the launcher app
list for the current user; a registered `LauncherApps.Callback` (`onPackageAdded` / `onPackageRemoved`
/ `onPackageChanged`) re-queries and emits. Entries are sorted by `label` using a locale-aware
collator. Consumers: `AppDrawer`, pages 2–3, and dock labels.

`LauncherApps` is final/system and cannot be mocked directly, so it is wrapped by
`LauncherAppsSource`; tests substitute a fake implementation.

### Layout

`LayoutRepository` persists slots in DataStore as a serialized `HomeLayout` (JSON string). A
`isFirstRun` flag triggers a **default layout** on first launch that matches the reference design:
page 1 contains the widget slots in fixed positions; the dock contains four user-set or
most-recently-used apps. It emits `Flow<HomeLayout>`; `HomeScreen` renders it; M4 writes back
mutations.

### Time

`Clock.ticks()` emits immediately, then at each minute boundary, cancels while the display is off
(observing screen on/off), and re-emits on screen-on.

`DayProgress` is a **pure function**: given an `Instant` and a timezone, it returns
`hourIndex (0..23)`, `minutesFraction`, and `percent`.

- `DateCard` and `DayProgressCard` read `Clock` + `DayProgress`.
- `CalendarCard` reads the same clock for "today" and holds its own `displayedMonth` UI state.

### State ownership

One view model per screen:

- `HomeViewModel` — merges `LayoutRepository.layout`, `AppRepository.apps`, and `Clock` into
  `HomeUiState`. Exposes `openApp(componentName)` and `launchCalendar(date)`.
- `DrawerViewModel` — `AppRepository.apps` plus a search query → filtered `DrawerUiState`.
- `SettingsViewModel` — `SettingsRepository.settings` (minimal in this cycle; fully wired in M4).

### Opening apps and the calendar

- Apps: `LauncherApps.startMainActivity(component, user, bounds)`.
- Calendar dates: an `ACTION_VIEW` intent to the default calendar (via `CalendarContract`), with a
  fallback to the calendar app's main activity.
- System shortcuts (M4): `LauncherApps.getShortcuts()` + `startShortcut`.

---

## 5. Widget design details

### DateCard

Weekday, month, and a large day number. Reads `Clock`. Updates exactly at midnight (see §7).

### DayProgressCard

Title, a grid of 24 dots (filled = elapsed hours, hollow = remaining), and percent text. Filling is
whole-dot only: exactly `hourIndex` dots are filled, no partial fill on the current dot. Reads
`Clock` + `DayProgress`; re-renders each minute.

### CalendarCard

Current month label with previous/next chevrons, a weekday header row, and a 6×7 date grid. The
user's swipe changes `displayedMonth` (local UI state). Today's date is marked with a filled `ink`
circle and `primaryLight` text. Tapping a date calls `launchCalendar(date)`.

All three cards are Compose composables inside a shared `CardFrame` that owns the 24 dp radius,
16 dp padding, and fill color.

---

## 6. Design tokens

Derived from base `#9CD4DB`.

| Token | Hex | Usage |
|-------|-----|-------|
| primary | `#9CD4DB` | Base color, page background |
| primaryLight | `#DDF0F3` | Card / widget background |
| primarySoft | `#C4E5EA` | Secondary cards, calendar area |
| primaryDark | `#3F7F88` | Accent, filled progress dots |
| ink | `#0F2A2E` | Text, icons, selected-date marker |
| inkMuted | `#5B7C81` | Secondary text, hollow progress dots |

Dark tokens (defined now, not wired in this cycle): background `#0F2A2E`, card `#17383D`,
text `#DDF0F3`.

**Typography (system sans-serif):** large date 48 sp Medium; widget title 14 sp Medium; body 12 sp Regular.

**Shape and spacing:** card radius 24 dp; inner padding 16 dp; gap between cards 12 dp; screen margin
16 dp. No shadows — separation is by color only.

---

## 7. Error handling and edge cases

- **LauncherApps empty/unavailable** → drawer shows an empty state; no crash; retry on next callback.
- **Icon decode failure** → fall back to a generic outline glyph in the tile; never blank.
- **Slot references an uninstalled package** → render `Empty` and auto-prune on the next layout
  write; do not crash, do not remove other slots.
- **Clock/timezone change** → `ACTION_TIME_CHANGED`, `ACTION_DATE_CHANGED`, and
  `ACTION_TIMEZONE_CHANGED` force an immediate tick, so the date flips exactly at 00:00.
- **Rotation / config change** → state survives via view models + DataStore. Portrait is the
  designed orientation; landscape falls back to a simple centered grid (documented, not a focus).
- **Cold start** → first Compose frame renders from cached layout and app list; the full
  `LauncherApps` query completes asynchronously.

---

## 8. Testing strategy

- **Pure unit tests (JVM):** `DayProgress` math (00:00 = 0%, 23:59 ≈ 99.9%, hour index, DST and
  timezone cases); slot-map serialization round-trip; drawer search filtering; icon-mode resolution
  (which apps get an outline versus the original).
- **Fake-clock tests:** `Clock` backed by a test scheduler — assert the date card flips at 00:00 and
  the progress card ticks each minute, without real time elapsing.
- **Repository tests:** `AppRepository` against a fake `LauncherAppsSource`.
- **Compose UI tests (instrumented, emulator `soft_home_pixel`):** page 1 renders the three widgets
  and dock; the drawer opens on swipe-up and filters; tapping an icon invokes the open callback.
- **Manual acceptance on the user's device:** set as default home; pressing Home from another app
  returns to page 1; installing an app makes it appear in the drawer; colors match `#9CD4DB`.

---

## 9. Definition of done (M0–M2)

Installed on the user's device and set as default home; page 1 matches the Pencil design; the
drawer lists all apps and launches them; the three widgets show correct live data; emulator tests
pass.

---

## 10. Pencil artboard plan

Drawn after this spec is approved. Five artboards, each a phone frame (~390×844):

1. **Home — Page 1** (reference layout): date card, day-progress card, calendar card, notes/tasks
   placeholder area reserved, page indicator, dock.
2. **Home — Pages 2 & 3:** 4×6 icon grid, same dock and indicator.
3. **App Drawer:** search field + alphabetical grid.
4. **Settings:** single screen of settings rows (drawn for completeness; shipped later).
5. **Component sheet:** tokens, the three widget cards, an icon tile, and the dock.

Icon tiles use generic outline placeholder glyphs (lucide / Material Symbols Outlined), not real app
icons.

---

## 11. Follow-up cycles (not this spec)

- **M3:** Quick Notes and Daily Tasks as new `Slot` kinds + Room persistence.
- **M4:** drag-and-drop over the same slot map; long-press system shortcuts; settings wiring.
- **M5:** multi-OEM device testing, performance tuning, bug fixes.
- **Later:** dark-mode toggle.

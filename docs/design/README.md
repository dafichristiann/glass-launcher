# Minimalist Launcher — Design (M0–M2)

Companion to `docs/superpowers/specs/2026-02-14-minimalist-launcher-design.md`.

## Artboards

Exported PNGs live in `docs/design/`. All use base color `#9CD4DB`.

| File | Artboard | Contents |
|------|----------|----------|
| `ASjBV.png` | Home — Page 1 | Date card, Day Progress card, Calendar (today circled), Quick Notes, Daily Tasks, page indicator, dock |
| `wAmO8.png` | Home — Page 2 | 4-column app grid (8 apps), page indicator (dot 2), dock |
| `haW1O.png` | Home — Page 3 | 4-column app grid (6 apps), page indicator (dot 3), dock |
| `XHXN2.png` | App Drawer | Search bar + 3-column app grid |
| `cKApJ.png` | Settings | 5 settings rows (warna dasar, mode ikon, kolom, tema, dock) |
| `wOs85.png` | Design Tokens | 6 color swatches + hex, spacing/stroke spec |

The editable source is the Pencil canvas in `untitled.pen` (open in the Pencil app).

## Tokens

| Token | Hex | Use |
|-------|-----|-----|
| primary | `#9CD4DB` | page background |
| primaryLight | `#DDF0F3` | cards, dock, icon tiles, search bar |
| primarySoft | `#C4E5EA` | calendar card, notes/tasks cards |
| primaryDark | `#3F7F88` | accent, day-progress label, color swatch |
| ink | `#0F2A2E` | text, outline icons, today marker |
| inkMuted | `#5B7C81` | secondary text, hollow dots, chevrons |

Geometry: card radius 24 dp, inner padding 16 dp, gap 12 dp, screen margin 16 dp. Icon tiles:
58 dp box, radius 17 dp, glyph 28 dp. Dock: 84 dp, 4 slots, tile box 50 dp radius 15 dp.
Outline icons: stroke 1.6 dp, `ink`, rounded caps. No shadows.

## Notes for implementation

- Icons are outline glyphs (stroke, no fill). In the app these map to `IconProvider`'s curated
  outline set; unmapped apps fall back to a tinted monochrome rendering of the real icon.
- The day-progress widget uses 24 dots, whole-dot fill (9 filled at 38% ≈ 09:00), percent text,
  and "9 of 24 hours" caption.
- Today's date uses an `ink` filled circle with `primaryLight` text.
- Page indicator: active dot `ink`, inactive `inkMuted`.
- Settings is drawn for completeness but ships in a later cycle (M4/M5).

## Environment note

The Pencil app used for this design did not persist edits to `untitled.pen` on disk during the
session (the file remained at its original empty state), and node-level screenshots intermittently
dropped children of large frames. The exports above were produced with the app's `Export` function,
which rendered correctly. If `untitled.pen` reopens empty, re-create the artboards from this doc
and the spec, or use the PNGs as the reference.

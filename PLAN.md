# Sudoku Android App — Plan

**Current stage:** Phase 7 complete. v1 done.
**Last updated:** 2026-05-06

## Scope (v1)

Offline single-player, classic 9×9, 4 difficulties (Easy/Medium/Hard/Expert), unique-solution generator, tap-cell→tap-number input, pencil notes, unlimited undo, mistake highlighting (peer conflicts by default; strict vs solution opt-in), timer w/ pause, auto-save & resume, stats, Material 3 dark/light theme.

**Defaults chosen:**
- Portrait only, phone only, English only
- min SDK 26 (Android 8.0), target SDK 35
- One active game at a time
- Input: select cell first, then number

## Stack

- Kotlin 2.x + Jetpack Compose (BOM 2026.xx)
- MVVM: ViewModel + StateFlow
- Hilt (DI), Room (persistence), kotlinx.serialization (board as JSON blob)
- Navigation Compose with type-safe routes
- AGP 8.x / Gradle 8.x / Kotlin DSL
- Testing: JUnit5, Kotest (property-based for solver), Turbine, Compose UI Test, MockK

## Structure

Single-module `:app`. Key rule: `domain/` has zero Android imports.

```
domain/   model, generator (Solver, PuzzleGenerator), usecase
data/     db (Room), repository, serialization
ui/       home, game, stats, settings, theme
di/       Hilt modules
```

## Phases (each = shippable milestone)

- [x] **Phase 0 — Skeleton** (½ day): Android Studio "Empty Activity (Compose)" project, add Hilt/Room/serialization deps, NavHost w/ placeholder, package skeleton, `.gitignore`, first commit.
- [x] **Phase 1 — Static grid renders** (1d): `Board`/`Cell` model, `GridView` composable (9×9, thick borders every 3), hard-coded puzzle, cell-tap selection, non-functional `NumberPad`.
- [x] **Phase 2 — Generator + solver** (2-3d): backtracking `Solver` returning 0/1/2+ solutions; `PuzzleGenerator` via dig-with-uniqueness-check; difficulty bands Easy 40-45 clues, Medium 32-36, Hard 28-31, Expert 24-27; unit + property-based tests.
- [x] **Phase 3 — Playable game** (2d): `GameViewModel` + `StateFlow<GameUiState>`, wire NumberPad, enforce given cells, win detection, "game won" dialog.
- [x] **Phase 4 — Notes, undo, highlighting** (2d): notes mode toggle, auto-remove notes setting, snapshot-based undo stack, peer-conflict detection, row/col/box + same-digit highlight.
- [x] **Phase 5 — Persistence + resume** (1d): Room DB (`games`, `settings` tables), debounced save on state change, "Resume" entry on home screen.
- [x] **Phase 6 — Timer, stats, settings** (1-2d): `Flow<Duration>` timer paused via Lifecycle, stats screen (completed games + best times), settings screen, Material 3 theming.
- [x] **Phase 7 — Polish + release** (1-2d): adaptive icon, Android 12 splash, R8 rules, signed release APK, optional Play Console internal track.

**Total: ~10-14 focused days.**

## Key architectural decisions

- **Generator:** randomized backtracking full-board → dig cells verifying uniqueness. Pre-generate 5/difficulty on idle, cache in Room so user never waits. Defer Dancing Links to v2.
- **State:** single immutable `GameUiState` with reducer-style actions. Undo = `history.push(state)`.
- **Schema:** `games(id, difficulty, board_json, history_json, elapsed_ms, created_at, updated_at, completed_at)` — `completed_at IS NULL` means active. `settings(key, value)`.

## Testing targets

- 90%+ on `domain/` (solver/generator via example + property-based tests)
- 60%+ on ViewModels (reducer tests are pure functions)
- Smoke-level Compose UI tests via `createComposeRule()`
- Room round-trip tests (in-memory)

## Deferred to v2

Cloud sync, daily puzzle, leaderboards, hints, Killer/16×16 variants, landscape/tablet, widget, Wear OS, i18n, ads/IAP, human-technique difficulty rating, Dancing Links solver, accessibility polish beyond semantic defaults.

## Open questions before Phase 0

- Who scaffolds the Android Studio project — you via the wizard (recommended for correct AGP/Kotlin versions) or me from CLI?
- Confirm final GitHub repo name (was `sudosu_app`, you said you renamed on remote).

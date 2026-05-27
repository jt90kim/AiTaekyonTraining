# Handoff: Taekyon Trainer — Android Shell UI

## Overview

This is a **design handoff** for the Android shell of the Taekyon Trainer app. The shell is the Kotlin/Compose layer that wraps the existing Unity scene: setup (LauncherActivity) and the transparent training overlay (MainActivity).

This bundle contains the visual design only — Unity, the motion library, and the bridge protocol already exist and are not in scope for this work.

## About the design files

The files in this bundle are **design references created in HTML** — interactive prototypes showing intended look and behavior. They are **not** production code to copy.

Your job is to **recreate these designs in the existing Android project at `claude/TaekyonClaude/`** using **Kotlin + Jetpack Compose**. Match the project's existing patterns (Gradle setup, AndroidX, Material 3 substrate). The HTML is a pixel-accurate visual spec — lift colors, dimensions, and copy from this README and the prototype, but build the screens idiomatically in Compose.

## Fidelity

**High-fidelity.** Final colors, typography, spacing, radii, and copy are committed. Recreate pixel-accurate where possible. Where Android conventions differ (e.g. system status bar, edge-to-edge insets), follow Android norms.

## Architecture context (already built — do not duplicate)

The user's project structure (read the project's `CLAUDE.md` files for full detail):

```
claude/
├── TaekyonClaude/    # Android project — YOU work here
├── Unity/            # Unity 6 project — already complete
├── unity-export/     # auto-generated; off-limits
└── tools/            # MediaPipe capture pipeline; not your concern
```

Off-limits inside `TaekyonClaude/`: `build/`, `.idea/`, `.git/`, `.gradle/`, `gradle/`.

The Android shell currently has only stock theme files in `res/values/`. No `MainActivity.kt` or `LauncherActivity.kt` exists yet. You are building those.

### Bridge protocol — already wired on the Unity side

Android → Unity (two methods on the `AndroidBridge` GameObject):

| Method | When | Payload |
|---|---|---|
| `SetEnabledMoves(csv)` | After `onUnitySceneReady`, before timer starts | Comma-separated variant IDs, e.g. `"roundhouse_low"` |
| `ReceiveMotionMessage(json)` | Legacy direct-playback path | Raw motion-clip JSON. Not used in current flow. |

Unity → Android (one callback, posted on Unity's `Start()`):

```kotlin
fun onUnitySceneReady()   // your MainActivity must implement this
```

**Handshake sequence MainActivity must implement:**

1. MainActivity loads, embeds Unity as a child view, shows transparent Compose overlay.
2. Unity calls `activity.onUnitySceneReady()` (off-main-thread — switch to main).
3. Compose starts the countdown timer and calls:
   `UnityPlayer.UnitySendMessage("AndroidBridge", "SetEnabledMoves", enabledMovesCsv)`
4. Timer counts down. At zero, overlay color shifts to `warn` orange. Unity keeps running.
5. User taps the exit button → finish MainActivity, return to LauncherActivity.

### Motion type ID convention (this matters)

The bridge expects **variant IDs** in the CSV, **not technique family IDs**. The format is `{technique}_{height}`, e.g. `roundhouse_low`, `roundhouse_high`, `front_low`.

Today the only ready variant is `roundhouse_low` (4 stance/leg permutations exist in `app/src/main/assets/motions/`). The others are in the catalog as planned roadmap items.

Verify with the user that Unity's `MoveVariant.moveType` array uses these same IDs — there is a known gap flagged in their spec where Android-side IDs may not match Unity-side filtering.

---

## Screens

Three top-level destinations. Use `androidx.activity.compose.setContent { … }` + a single Navigation graph, or two Activities (`LauncherActivity` for Splash + Setup, `MainActivity` for Training) — match whatever existing pattern the project sets. The spec calls for two Activities.

### 1 · Splash — `LauncherActivity` start destination

**Purpose:** Brand intro + entry point. Tap CTA → navigate to Setup.

**Layout (412dp × full height, edge-to-edge, no system bars):**

```
┌─ padding 22dp 18dp ───────────────────────────────────┐
│  [top meta strip: "B · EMBERS" left, "v0.4.1" right]   │
│                                                         │
│  ┌─ center block, padding 0 28dp, vertical center ─┐  │
│  │  결련택견              ← Noto Sans KR 22sp / 500 / accent
│  │                       margin-bottom 14dp          │
│  │  TAEKYON              ← Space Grotesk 64sp / 600
│  │                       letter-spacing -0.04em      │
│  │                       line-height 0.92            │
│  │  ─────  (48dp × 2dp accent rule, margin-top 16dp) │
│  │  Trainer · Korean martial reaction drill          │
│  │                       Space Grotesk 17sp / mute   │
│  │                       margin-top 14dp             │
│  │                                                    │
│  │  [About card, see below]   margin-top 40dp         │
│  └────────────────────────────────────────────────────┘ │
│                                                         │
│  [Primary CTA "Begin training"]    bottom, 22dp 30dp   │
└─────────────────────────────────────────────────────────┘
```

**About card:** rounded rectangle, `surface` background, 1dp `line` border, 18dp radius, padding 18dp 18dp 16dp. Contains:
- Label "About" — Geist Mono 10sp / mute / letterspacing 0.06em
- Body — Space Grotesk 13sp / mute / line-height 1.55: "Watch the opponent. React in real time. No scoring. No pose tracking. Just rhythm."

**Primary CTA:** full-width, 60dp tall, pill-shaped (radius full), `accent` background, `accentInk` text, Space Grotesk 16sp / 600. Label "Begin training". `onClick` → start `LauncherActivity`'s Setup screen.

### 2 · Setup — `LauncherActivity` setup destination

**Purpose:** Pick session duration + technique variants. Tap Start → launch `MainActivity` with these as Intent extras.

**State:**
- `seconds: Int` — clamped `[15..300]`, default `60`
- `enabledMoves: Set<String>` — default `setOf("roundhouse_low")`

**Layout:**

```
┌─ Header (18dp 20dp 16dp) ─────────────────────────────┐
│  [back ←]      "Session setup"           [spacer 32dp] │
│                 훈련 준비                                │
├────────────────────────────────────────────────────────┤
│  Scrollable body (20dp horizontal padding):             │
│                                                         │
│  Duration section                                       │
│  ─ Big timer display card (1:00)                        │
│  ─ Preset chip row [0:30][1:00][2:00][3:00][5:00]       │
│                                                         │
│  Techniques section                                     │
│  ─ Roundhouse card (full, with height chips)            │
│  ─ Front Kick row (compact, "soon")                     │
│  ─ Side Kick row (compact, "soon")                      │
│  ─ Hook Kick row (compact, "soon")                      │
│  ─ Footer hint card (dashed border, "1 ready · 7        │
│    planned")                                            │
├────────────────────────────────────────────────────────┤
│  Footer Start CTA (14dp 20dp 22dp)                      │
└────────────────────────────────────────────────────────┘
```

**Header:** 32dp icon-button back arrow on left, two-line center block (title 15sp / 600 over `훈련 준비` 10sp KR / mute2), 32dp spacer on right.

**Duration card:**
- Background `surface`, 1dp `line` border, 18dp radius, padding 20dp 22dp.
- Three columns: `[ –15s ]   1:00   [ +15s ]`
  - Stepper buttons: 56×32dp, transparent, 1dp `line` border, 12dp radius, Geist Mono 11sp / mute.
  - Center display: Geist Mono 64sp / 600 / `fg`, tabular-nums, letter-spacing -0.04em.
- Stepper buttons clamp to `[15..300]`.

**Preset chips:** 5 buttons in a row, each 36dp tall, equal flex. Active = `accent` fill + `accentInk` text. Inactive = transparent + `line` border + `fg` text. Geist Mono 12sp / 600. Selecting a preset overrides `seconds`.

**Technique catalog** — see "Motion catalog" below for full data. Two visual states:

- **Ready family** (Roundhouse only today): Full card with height chips inside. `surface` bg if no variants enabled, `accentDim` bg + 1dp `accent` border if any variant enabled.
- **Planned family**: Compact one-line row, 0.55 opacity, fixed `line` border. Just name + hangul + romaja + a `SOON` pill on the right. Not interactive.

**Height chip anatomy:** small button per height. Layout: `[checkbox 14dp] [Label "Low"] [variant count "· 4V" mono small]`. On = `accent` bg + `accentInk` text. Off = transparent + `lineStrong` border. Disabled (`status = soon`) = transparent + `line` border + 0.6 opacity + cursor not-allowed + label trailing reads `· SOON` instead of `· 4V`.

**Footer hint card** under the technique list: dashed 1dp `line` border, 12dp radius, padding 12dp 14dp. Geist Mono 10sp / mute2 / line-height 1.5. Prefixed by a `◆` glyph in `accent`. Copy:

> "New variants unlock as motion clips are captured. Current build: 1 ready · 5 planned."

**Start CTA:** full-width 60dp pill, `accent` bg, `accentInk` text. Label `Start · 1:00` (dynamic, shows current selected duration). If `enabledMoves.isEmpty()` → disabled state: `surface2` bg, `mute2` text, label "Pick at least one variant".

On Start: launch `MainActivity` with `seconds` and `enabledMoves` as Intent extras.

### 3 · Training overlay — `MainActivity`

**Purpose:** Transparent Compose overlay over the embedded Unity SurfaceView.

**View structure (Z-order, back to front):**
1. Unity SurfaceView, full bleed.
2. (No additional background — let Unity show through.)
3. Loading veil — `bg`-with-alpha + 4dp blur, while `unityReady = false`. Spinner (40dp, 2dp ring, `accent` top arc, rotates 0.9s linear infinite) + `mute` label "Preparing opponent…".
4. Top edge chrome row.
5. Bottom edge chrome row (kept minimal here — EMBERS shows nothing at the bottom).

**Top edge chrome (14dp from top, 14dp horizontal):** three items in a row, `space-between`. None of them should darken the Unity scene heavily — backgrounds are `surface` with low opacity + 8dp backdrop blur where supported.

| Slot | Size | Anatomy |
|---|---|---|
| Exit (top-left) | 44×44dp pill | `surface` bg, 1dp `line` border, `fg` "✕" icon (16dp, stroke 1.6dp). On tap: `finish()` the activity. |
| Timer (center) | min 140dp wide × auto | Pill, `surface` bg, 1dp `line` border. Two stacked rows: a 9sp Geist Mono status label ("Active" / "Preparing opponent" / "Time up · keep watching"), and a 30sp Geist Mono / 600 / tabular-nums M:SS countdown. |
| Kicks (top-right) | min 44dp × 44dp pill | `surface` bg, 1dp `line` border. Two stacked rows: 9sp mono "kicks" label, 14sp Geist Mono / 600 / `accent` count. The count is local to the overlay — increment whenever Unity fires a kick. (If Unity doesn't emit a kick callback today, fake it with a 1.8s interval timer for now; flag this for later.) |

**Time-up behavior:** when `remaining` reaches 0, set `overTime = true`:
- Timer pill border → `warn` color.
- Timer status label → "Time up · keep watching" (`warn` color).
- Exit button background → `warn` (whole button becomes the warn fill), icon → `accentInk`.
- Kicks counter color → `warn`.
- Unity scene gets a subtle `grayscale(0.4)` filter applied as a CSS-equivalent. On Android, fade the overlay so the Unity scene gets a visual "muted" cue — easiest: animate a 12% `bg`-colored scrim on top of the Unity view.

**Behavior:**
- Timer is suspended until `unityReady` is true (gating on the bridge callback).
- After ready, post `SetEnabledMoves` once, then start the per-second decrement loop.
- Timer continues to decrement past 0 going negative is NOT desired — clamp to 0 and stay there. Unity keeps running indefinitely.
- Exit at any time returns to LauncherActivity.

---

## Motion catalog (drives the Techniques section)

This is the catalog the Setup screen renders. Keep this in a Kotlin object (e.g. `MotionLibrary.kt`) for now; eventually the user wants this driven by what's actually in `app/src/main/assets/motions/`.

```kotlin
data class TechniqueFamily(
  val id: String,           // "roundhouse", "front", ...
  val name: String,         // English display name
  val hangul: String,       // 돌려차기
  val romaja: String,       // Dollyeo-chagi
  val desc: String,         // short description (Setup full card)
  val status: Status,       // Ready or Soon
  val heights: List<HeightVariant>
)

data class HeightVariant(
  val id: String,           // "roundhouse_low" — this is the CSV token
  val label: String,        // "Low", "High"
  val variants: Int,        // count of stance/leg permutations captured
  val status: Status        // Ready or Soon
)

enum class Status { Ready, Soon }
```

Catalog content:

| Family | Hangul | Romaja | Description | Status | Heights |
|---|---|---|---|---|---|
| Roundhouse | 후려차기 / 돌려차기 | Huryeo-chagi / Dollyeo-chagi | Circular sweep with the shin · whole-body rotation | Ready | `roundhouse_low` (4V, Ready), `roundhouse_high` (4V, Soon) |
| Front Kick | 앞차기 | Ap-chagi | Straight thrust off the lead leg | Soon | `front_low` (4V, Soon), `front_high` (4V, Soon) |
| Side Kick | 옆차기 | Yeop-chagi | Linear push from the hip · long range | Soon | `side_low` (4V, Soon), `side_high` (4V, Soon) |

Wire `enabledMoves` to the union of `HeightVariant.id` values whose checkbox is on. CSV when sending to Unity: `enabledMoves.joinToString(",")`.

---

## Design tokens

All values are dark-mode defaults. The HTML prototype includes a light mode too (Tweaks panel); ship dark first and only add light mode if the user explicitly asks for it.

### Color

| Token | Hex | Usage |
|---|---|---|
| `bg` | `#100C08` | Window background, scaffold root |
| `surface` | `#1A140E` | Cards, pills, raised surfaces |
| `surface2` | `#241B12` | Disabled CTA background |
| `line` | `rgba(255, 220, 170, 0.10)` | Hairline borders |
| `lineStrong` | `rgba(255, 220, 170, 0.25)` | Form-control borders (checkbox unchecked) |
| `fg` | `#F6EAD6` | Primary text |
| `mute` | `rgba(246, 234, 214, 0.55)` | Secondary text |
| `mute2` | `rgba(246, 234, 214, 0.32)` | Tertiary / metadata text |
| `accent` | `#FFAA2B` | Brand amber. Active CTA, accent rules, selected states. |
| `accentDim` | `rgba(255, 170, 43, 0.16)` | Selected-card fill |
| `accentInk` | `#1A0E00` | Text on `accent` fills (deep brown, not pure black) |
| `warn` | `#FF6A3A` | Time-up state |
| `danger` | `#FF3A4D` | Reserved; not used in current flows |

### Typography

Three families. Add to `app/build.gradle.kts`:

```kotlin
implementation("androidx.compose.ui:ui-text-google-fonts:<version>")
```

Then load via `GoogleFont` provider:

- **Space Grotesk** — display + body sans (`fonts.sans` and `fonts.display`)
- **Geist Mono** — all numerals and mono labels (`fonts.mono`)
- **Noto Sans KR** — Korean accent text (`fonts.kr`)

Type ramp (sp values map 1:1 to the px sizes used in HTML; weight matches `fontWeight` in Compose):

| Role | Family | Size | Weight | Tracking | Used in |
|---|---|---|---|---|---|
| Display | Space Grotesk | 64sp | 600 | -0.04em | Splash wordmark |
| Numeric large | Geist Mono | 64sp | 600 | -0.04em | Duration display |
| Numeric mid | Geist Mono | 30sp | 600 | -0.02em | Training timer |
| Title | Space Grotesk | 16sp | 600 | -0.01em | Technique name |
| Body | Space Grotesk | 12 / 13 / 17sp | 400 | 0 | Descriptions, subtitles |
| Label | Geist Mono | 10 / 11sp | 500 | 0.06–0.18em | Section labels, statuses |
| KR | Noto Sans KR | 10 / 13 / 22sp | 500 | 0.04em | Hangul accents |

### Spacing scale

`4, 8, 12, 16, 20, 24, 32 dp`. Use `dp` throughout; never `sp` for layout.

### Radius

| Token | Value |
|---|---|
| `sm` | 6dp |
| `md` | 12dp |
| `lg` | 18dp |
| `pill` | full (use `RoundedCornerShape(50)` or `CircleShape` for height-bounded items) |

### Shadow

Single shadow used on the splash CTA only — match Material 3's `level 2` elevation, no custom shadow stack needed elsewhere.

---

## Interactions & behavior

- **Transitions:** prefer Compose's default `AnimatedContent` for screen-level changes; no custom motion. Screen-jump tabs in the HTML prototype are dev-helpers — do not replicate.
- **Hover:** N/A on mobile.
- **Active / pressed states:** Compose's default ripple is fine. Tint = `accent` at 12% opacity on dark surfaces.
- **Focus:** N/A; this is touch-only.
- **Loading:** Training screen shows a spinner overlay until `onUnitySceneReady()` fires.
- **Validation:** Setup's Start CTA disables when no variants are enabled.
- **Edge-to-edge:** opt in via `WindowCompat.setDecorFitsSystemWindows(window, false)`. Training Activity needs full edge-to-edge so Unity occupies the whole screen.

## Assets

- **Icons:** all SVGs in the HTML prototype are simple line glyphs (back chevron, X close). Recreate as Compose `Canvas` lambdas or use `androidx.compose.material.icons.outlined.Close` / `ArrowBack` — both are fine.
- **Skeleton silhouette:** the stick figure visible in the Training screen of the prototype is a placeholder for the Unity scene. **Do not implement it.** It's just a backdrop so the timer chrome has something to sit on top of in the prototype.
- **No images, no logos.** Wordmark is set in Space Grotesk type.

## Files in this bundle

- `prototype.html` — bundled, self-contained interactive prototype. Open in any modern browser. Use the `01 · 02 · 03` chips at the bottom of each phone artboard to jump between screens.
- `reference/themes.jsx` — source-of-truth for THEMES (color/type/radius) and TECHNIQUES catalog.
- `reference/screens.jsx` — source-of-truth for screen layouts and component anatomy.
- `reference/tokens-sheet.jsx` — token spec sheet (mirrors the dedicated handoff artboard in the prototype).
- `reference/app.jsx` — design-canvas wrapper. Useful for understanding which artboards exist; not relevant to the Android code.

## Open questions for the user

These came up during the design and are worth confirming before you start writing Compose:

1. **Move type ID alignment.** The catalog uses `{technique}_{height}` (e.g. `roundhouse_low`). Verify this matches Unity's `MoveVariant.moveType` strings. Current spec notes a known gap where Android-side IDs may not align with Unity's filtering.
2. **Kicks counter source.** The overlay shows a live count of kicks performed. Should this come from a Unity callback (preferred — add a `UnitySendMessage` from Unity → Android) or be timer-faked from Android? The prototype fakes it.
3. **Light mode.** The HTML prototype supports both dark and light. The user said dark-first. Ask whether light is in scope or backlog.
4. **Time-up behavior.** Today the prototype keeps Unity running and just shifts colors to `warn`. The original spec also says "overlay turns orange when done." Confirm this is the desired behavior vs. auto-exiting at 0:00.

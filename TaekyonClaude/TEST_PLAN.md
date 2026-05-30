# Taekyun Trainer — Device Test Plan

### 1. Install & Launch

| # | Check | Pass criteria |
|---|-------|---------------|
| 1.1 | Install fresh (or clear app data) | No crash on first launch |  done
| 1.2 | System splash appears | Amber kicking figure on `#100C08` background | done
| 1.3 | Splash duration | Splash holds for ≥ 1500 ms before transitioning | done
| 1.4 | Splash exit animation | Fades out over ~600 ms; does not snap away | good enough
| 1.5 | App icon on home screen | Amber figure, correct background; round variant also correct | yes
| 1.6 | Android 13+ themed icon | Monochrome silhouette shows when system accent is applied |   works

---

### 2. Setup Screen

| # | Check | Pass criteria |
|---|-------|---------------|
| 2.1 | Default state | Duration = 1:00 (60 s), `roundhouse_low` chip checked |   done
| 2.2 | Duration stepper | –15 s / +15 s increments correctly; floor = 0:15 (15 s), ceiling = 5:00 (300 s) | done
| 2.3 | Preset chips | Tapping 0:30 / 1:00 / 2:00 / 3:00 / 5:00 snaps duration; active chip highlights amber |  done
| 2.4 | Start button label | Shows "Start · 1:00" (or current duration) when ≥ 1 variant is selected | done
| 2.5 | Deselect all variants | Start button changes to "Pick at least one variant" and is non-tappable | done
| 2.6 | Re-select after deselecting | Start button re-enables | done
| 2.7 | "Soon" badge | All 4 variants are Ready — confirm no "SOON" chips appear | done
| 2.8 | Technique count note | Shows "8 ready · 0 planned" | done
| 2.9 | Technique card highlight | Card gets amber border + tint when any height chip is on |  done

---

### 3. Training Screen — Loading State

| # | Check | Pass criteria |
|---|-------|---------------|
| 3.1 | Loading veil appears immediately | Spinner visible, "Preparing opponent…" label, status label shows "Loading…" |
| 3.2 | Timer does not count down during loading | Timer holds at the session duration (e.g., 1:00) until Unity calls `onUnitySceneReady()` |
| 3.3 | Veil fades out on Unity ready | ~300 ms animated fade-out; Unity scene becomes visible underneath |
| 3.4 | Timer pill updates to "Active" | Status label changes from "Loading…" to "Active"; countdown begins |

---

### 4. Training Screen — Active State

| # | Check | Pass criteria |
|---|-------|---------------|
| 4.1 | Timer accuracy | Counts down 1 s per tick; verify 10 s elapsed = 10 ticks against a real clock |
| 4.2 | Amber warning | Timer text and border change to amber color when remaining time < 10 s |
| 4.3 | Kick counter | Shows "00" at start; increments approximately once every 1800 ms (simulated) |
| 4.4 | Exit (×) button | Tapping returns to setup screen |
| 4.5 | Hardware back button | Also returns to setup screen |
| 4.6 | Unity scene running | Mannequin performs kicks and stance transitions autonomously |

---

### 5. Session Complete Card

| # | Check | Pass criteria |
|---|-------|---------------|
| 5.1 | Card appears at 0:00 | Fades in (~300 ms) when timer reaches 0 |
| 5.2 | Timer pill | Status label shows "Time up"; timer value and pill border turn amber |
| 5.3 | DURATION stat | Displays the session duration that was set (e.g., "1:00" for a 60 s session) |
| 5.4 | KICKS stat | Displays accumulated kick count in amber |
| 5.5 | TECHNIQUE list with one technique | One line, e.g., "Roundhouse Low" |
| 5.6 | TECHNIQUE list with multiple techniques | One line per enabled move ID; all selected techniques appear |
| 5.7 | Done button | Tapping navigates back to setup screen |
| 5.8 | Setup screen state on return | Duration and technique selections match what was configured, not reset to defaults |
| 5.9 | Second session | Start again → loading veil appears, Unity fires `onUnitySceneReady()`, countdown resumes correctly |

---

### 6. Android ↔ Unity Bridge

| # | Check | Pass criteria |
|---|-------|---------------|
| 6.1 | `onUnitySceneReady` fires | Loading veil disappears within a few seconds of scene load |
| 6.2 | `SetEnabledMoves` — single type | Enable only `roundhouse_low` → opponent performs roundhouse only; splint kicks do not appear |
| 6.3 | `SetEnabledMoves` — both roundhouse variants | Opponent performs both low and high roundhouse |
| 6.4 | `SetEnabledMoves` — splint only | Opponent performs only splint / 내차기 kicks |
| 6.5 | `SetEnabledMoves` — all 4 variants | Opponent cycles through all kick types |

---

### 7. Theme

| # | Check | Pass criteria |
|---|-------|---------------|
| 7.1 | Light mode | All screens readable; amber accents, light backgrounds, dark text |
| 7.2 | Dark mode | All screens readable; amber accents, dark backgrounds, light text |
| 7.3 | System theme switch | Change device theme while app is backgrounded; re-open to see updated colors |

---

### 8. Korean Locale

_Translation strings being updated — test cases to be written after strings are finalised._

---

### 9. Known Limitation (not a bug)

The kick counter increments on a fixed 1800 ms timer in the UI layer. It is not wired to actual Unity kick events, so the displayed count is an approximation and will not match the real kick cadence exactly. This is expected behavior.

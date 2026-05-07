# CLAUDE.md — Taekyon Training App (Root)

## Project Goal

Unity 6 application where an AI opponent performs pre-authored Taekyon (결련택견) motions.
The user observes and reacts. No real-time user detection or pose tracking.

## Workspace Structure

```
claude/
├── TaekyonClaude/    # Android project — motion JSON library + bridge UI
├── Unity/            # Unity 6 project — motion playback engine
├── tools/            # Offline capture tools
│   ├── extract_motion.py   # MediaPipe → JSON extractor (run offline)
│   └── capture_guide.md    # Video recording + script usage instructions
├── unity-export/     # ⛔ AUTO-GENERATED — never read or modify
└── taekyon_training_spec.md
```

## ⛔ Off-Limits Directories

| Path | Reason |
|------|--------|
| `unity-export/` | Unity build output, 100% auto-generated |
| `TaekyonClaude/build/` | Android build output |
| `TaekyonClaude/.idea/` | IDE metadata |
| `TaekyonClaude/.git/` | Git internals |
| `TaekyonClaude/.gradle/` | Gradle cache |
| `TaekyonClaude/gradle/` | Gradle wrapper |

## Architecture Overview

```
MediaPipe (offline, tools/extract_motion.py)
    ↓ JSON motion files
Android (TaekyonClaude) — move type catalog + user selection UI
    ↓ UnitySendMessage "SetEnabledMoves" (CSV of move type IDs)
AndroidBridge.cs (Unity)
    ↓
MotionStateMachine.cs — data-driven: stances, transitions, move variants
    ↓
MotionLoader.cs → MotionPlayer.cs → SkeletonMapper.cs → DebugSkeletonRenderer.cs
```

## Critical Constraint

All motion data MUST be captured via MediaPipe offline and exported as JSON.
Unity ONLY plays back motion data — it never generates or synthesizes movement.

## Milestones

| Milestone | Scope | Status |
|-----------|-------|--------|
| 1 — Hello Skeleton | MotionClip, MotionLoader, MotionPlayer, SkeletonMapper, DebugSkeletonRenderer | ✅ Done |
| 2 — Android in Control | AndroidBridge + Android UI sends JSON to Unity | ✅ Done |
| 2.5 — Post-M2 Polish | Timer delay, back button, skeleton shader fix, dual-app install fix | ✅ Done |
| 3 — Smooth Moves | Motion blending (0.2–0.4s LERP between clips) | ✅ Done |
| 4 — Fighter's Rhythm | State machine + autonomous step loop + kicks | ✅ Done |
| 5 — Polish | Timing, anticipation, flow refinement | ✅ Done |
| 6 — Real Kicks | Capture kick clips via MediaPipe; wire into data-driven state machine | 🔄 Recapturing |

## Current Status

**Milestone 6 clips captured and wired.** All 11 clips have been extracted and assigned in the Unity Inspector. Motion quality is acceptable but the kick clips need recapture — the initial takes were sloppy and the kick motion is hard to read on screen. Recapture planned.

### What shipped (M6)

**`MotionStateMachine.cs` — full rewrite (data-driven):**
- Replaced hardcoded 5-state machine with configurable arrays: `StanceDefinition[]`, `TransitionDefinition[]`, `MoveVariant[]`
- Fixed alternating stance cycle: neutral → left_forward → neutral → right_forward → neutral → …
- Moves only fire from a forward stance (never from neutral)
- Move selection: filters `MoveVariant[]` by current stance + enabled move types, then rolls probability
- After a move, fighter returns to the same stance idle (not neutral)
- `SetEnabledMoves(string csv)` public method — called by AndroidBridge when Android sends selection
- In `Start()`, seeds `_enabledMoveTypes` from all configured `moveVariants` — ensures kicks fire in the Unity Editor without Android calling `SetEnabledMoves`

**`AndroidBridge.cs` — added `SetEnabledMoves` handler:**
- Android calls `UnitySendMessage("AndroidBridge", "SetEnabledMoves", "roundhouse_low,split_kick_low")`
- Forwards to `MotionStateMachine.SetEnabledMoves()`

**`MotionLibrary.kt` — rewritten with typed move catalog:**
- `data class MoveType(val id: String, val displayName: String)`
- Explicit catalog replaces filename glob filter; IDs match `moveType` field in Unity Inspector
- `LauncherActivity` reads this to populate selection UI; selected IDs sent to Unity as CSV

**`tools/extract_motion.py` — MediaPipe capture script (upgraded):**
- Input: video file. Output: `{ fps, frames[] }` JSON in project format
- Outputs at **15 fps** (was 8 fps); extracts 19 joints; converts axes to Unity space
- **Auto facing correction:** detects filming angle from average shoulder vector, rotates all frames so skeleton faces +X. No manual angle input needed. Prints correction angle in the sanity output.
- Sanity printout: nose y range, ankle y range, y offset applied, facing correction degrees

**`MotionPlayer.cs` — now self-contained:**
- Moved frame ticking into its own `Update()` — no longer depends on `MotionTimeController`
- Moved `SkeletonMapper` subscription into its own `Start()` — no longer requires external wiring

**`MotionTimeController.cs` — deprecated:**
- Replaced by `MotionPlayer`'s own `Update()`. Kept as empty shell so the scene component slot isn't lost.

**`SampleScene.unity` — Inspector wired via direct YAML edit:**
- Stances (3), Transitions (4), MoveVariants (4) arrays populated with all clip GUIDs
- The YAML still had old field names (`neutralClip`, `leftStepClip`, etc.) from the previous MotionStateMachine version; these were replaced with the new array structure

**`tools/extract_motion.py` + all 11 clips:**
- All clips extracted, grounded, and facing-corrected
- Placed in `Unity/UnityTaekyon/Assets/SampleMotions/` and `TaekyonClaude/app/src/main/assets/motions/`

### Clips present but marked for recapture

All 11 clips exist and are wired. Idle and transition clips look good. The 4 kick variants need better takes.

| Filename | Type | State |
|---|---|---|
| `neutral_idle.json` | stance loop | ✅ Good |
| `left_forward_idle.json` | stance loop | ✅ Good |
| `right_forward_idle.json` | stance loop | ✅ Good |
| `neutral_to_left.json` | transition | ✅ Good |
| `left_to_neutral.json` | transition | ✅ Good |
| `neutral_to_right.json` | transition | ✅ Good |
| `right_to_neutral.json` | transition | ✅ Good |
| `roundhouse_left_front_low.json` | move variant | 🔄 Recapture |
| `roundhouse_left_rear_low.json` | move variant | 🔄 Recapture |
| `roundhouse_right_front_low.json` | move variant | 🔄 Recapture |
| `roundhouse_right_rear_low.json` | move variant | 🔄 Recapture |

Place new captures in **both**:
- `Unity/UnityTaekyon/Assets/SampleMotions/`
- `TaekyonClaude/app/src/main/assets/motions/`

The Inspector wiring (GUIDs) stays the same as long as filenames don't change — Unity tracks by GUID, not content.

### Move type naming convention
```
{move_type}_{stance}_{leg_role}_{height}.json
  stance   = left | right   (which foot is FORWARD)
  leg_role = front | rear   (which leg executes, relative to stance)
  height   = low | high     (target height of the kick)
```
`left`/`right` always describes the stance, never the kicking leg.

**Next:** Recapture the 4 kick variants per `tools/capture_guide.md`. Run `extract_motion.py`, overwrite the existing files. The Inspector wiring will pick up the new content automatically.

## Non-Goals

- Real-time pose tracking (MediaPipe is offline only)
- Procedural animation or IK
- Unity Animator / Mecanim
- Scoring or reaction timing
- Manual joint manipulation

# CLAUDE.md — Taekyun Training App (Root)

## Project Goal

Unity 6 application where an AI opponent performs pre-authored Taekyun (결련택견) motions.
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
| 6 — Real Kicks | Capture kick clips via MediaPipe; wire into data-driven state machine | ✅ Done |
| 7 — Visual Polish | Capsule renderer, color coding, floor grid, perspective camera | ✅ Done |
| 8 — Android App Polish | Session complete card, dark/light theme, i18n (KO), app icon, splash delay | ✅ Done |

## Current Status

**All milestones complete.** The opponent runs autonomously, performs kicks continuously, and the scene renders cleanly. The Android shell is fully polished: themed UI, Korean localisation, session report card, and a custom app icon.

### Scene configuration (SampleScene.unity)

| Setting | Value |
|---|---|
| Camera | Perspective, FOV 50, position (0, 1.25, -3), 5° downward tilt |
| Skeleton root Y rotation | 280° |
| Move probability | 95% |
| Idle duration | 0 (no pause between actions) |
| Transition speed | 2× |
| Move (kick) speed | 1.5× |

### Visual system (MannequinRenderer.cs)

Single procedural flat-shaded mesh rebuilt each frame from joint world positions.

- **Body:** warm neutral gray `(0.76, 0.74, 0.72)`, URP/Lit, smoothness=0 (fully matte)
- **Torso:** 4-level elliptical mesh (pelvis → waist → chest → clavicle, 10-sided, depth ratio 0.52)
- **Neck:** short frustum, shoulder midpoint → head base
- **Head:** featureless sphere r=0.095
- **Arms:** 8-sided tapered frustums (shoulder 0.050 → wrist 0.025); hand cone stub
- **Legs:** 9-sided tapered frustums (hip 0.062 → ankle 0.030); flat foot slab
- **Floor:** 8×8 black/white checkerboard plane (4m×4m) at y=0, visible in perspective

### All 11 motion clips

| Filename | Type | State |
|---|---|---|
| `neutral_idle.json` | stance loop | ✅ Good |
| `left_forward_idle.json` | stance loop | ✅ Good |
| `right_forward_idle.json` | stance loop | ✅ Good |
| `neutral_to_left.json` | transition | ✅ Good |
| `left_to_neutral.json` | transition | ✅ Good |
| `neutral_to_right.json` | transition | ✅ Good |
| `right_to_neutral.json` | transition | ✅ Good |
| `roundhouse_left_front_low.json` | move variant | ✅ Captured |
| `roundhouse_left_rear_low.json` | move variant | ✅ Captured |
| `roundhouse_right_front_low.json` | move variant | ✅ Captured |
| `roundhouse_right_rear_low.json` | move variant | ✅ Captured |

### Key implementation notes

- `extract_motion.py` facing correction is clamped to ±90° — prevents ~180° body flip from filming angle
- `SkeletonMapper.yRotationOffset` + root Transform rotation used to orient skeleton toward camera
- `MotionPlayer.Tick()` holds clip at frame 0 during blend (avoids mid-clip snap on transition)
- Anticipating state removed — Idling fires directly into `Fire()` when timer expires
- After a kick, fighter transitions immediately to neutral (no idle pause)
- Neutral stance passes through with timer=0 (fires instantly)
- `Screen.sleepTimeout = SleepTimeout.NeverSleep` set in `AndroidBridge.Start()` — prevents screen dimming during Unity session

### Move type naming convention
```
{move_type}_{stance}_{leg_role}_{height}.json
  stance   = left | right   (which foot is FORWARD)
  leg_role = front | rear   (which leg executes, relative to stance)
  height   = low | high     (target height of the kick)
```

### Android ↔ Unity integration status

| Item | Status |
|---|---|
| `onUnitySceneReady()` callback | ✅ Wired — Unity calls Android when scene loads; Android freezes opponent and starts 3-second countdown, then unpauses |
| `SetPaused(bool)` bridge | ✅ Wired — `UnitySendMessage("AndroidBridge","SetPaused","true"\|"false")` sets `Time.timeScale`; called on countdown end, pause button, exit dialog, and `onPause()` |
| `SetEnabledMoves(csv)` bridge | ✅ Wired — Android sends CSV of enabled move type IDs; Unity state machine filters variants |
| Move ID alignment | ✅ Confirmed — Android uses `roundhouse_low`, all 4 Unity `MoveVariant.moveType` fields are `roundhouse_low` |
| `roundhouse_high` in Android catalog | ✅ 4 variants captured and wired in scene; marked Ready in MotionLibrary.kt |
| `ReceiveMotionMessage` (direct JSON path) | 🗄️ Wired but unused — superseded by state machine |
| Android mannequin shader | ✅ Fixed — `MannequinMat.mat` asset forces URP Lit shader into build; wired via `_materialTemplate` |

### Android UI (Milestone 8)

| Feature | Detail |
|---|---|
| **Session complete card** | Fades in when timer hits 0. Shows DURATION + KICKS stats, technique list, Done button → back to launcher |
| **Dark / light theme** | `TaekyonColors` CompositionLocal; `isSystemInDarkTheme()` selects dark/light palette automatically |
| **Internationalisation** | `res/values/strings.xml` (English) + `res/values-ko/strings.xml` (Korean). `MotionLibrary` uses `@StringRes` IDs; all composables use `stringResource()` |
| **App icon** | Custom adaptive vector icon: amber kicking figure on dark brown `#100C08` background. Monochrome layer for Android 13+ themed icons |
| **Splash screen delay** | `SPLASH_DELAY_MS = 1500L` in `LauncherActivity.kt` — "Begin training" button fades in after this delay. Use a plain `val` (not `const val`) so hot-swap picks up changes |
| **3-second countdown** | After `onUnitySceneReady()`, Unity is paused and a full-screen "3 → 2 → 1" overlay counts down before the timer and opponent start |
| **Pause button** | Bottom-center amber pill with icon + "Pause"/"Resume" text; visible only after countdown finishes; hidden on session end |
| **Exit confirmation dialog** | Hardware back during training pauses Unity and shows "End training?" dialog; "Keep training" resumes; "End session" → setup screen |
| **Auto-pause on background/lock** | `onPause()` pauses Unity and shows Resume button; screen lock during countdown resets countdown to 3; no auto-resume on return |
| **Screen wake lock** | `FLAG_KEEP_SCREEN_ON` (Android) + `Screen.sleepTimeout = NeverSleep` (Unity) — screen stays on during training; manual power-button lock still works |
| **Back navigation** | `moveTaskToBack(true)` instead of `finish()` — avoids Unity killing the process; setup screen back exits app normally |

## Non-Goals

- Real-time pose tracking (MediaPipe is offline only)
- Procedural animation or IK
- Unity Animator / Mecanim
- Scoring or reaction timing
- Manual joint manipulation

# CLAUDE.md — Taekyon Training App (Root)

## Project Goal

Unity 6 application where an AI opponent performs pre-authored Taekyon (결련택견) motions.
The user observes and reacts. No real-time user detection or pose tracking.

## Workspace Structure

```
claude/
├── TaekyonClaude/    # Android project — motion JSON library + bridge UI
├── Unity/            # Unity 6 project — motion playback engine
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
MediaPipe (offline)
    ↓ JSON motion files
Android (TaekyonClaude) — stores motion library, sends JSON to Unity
    ↓ JSON string
AndroidBridge.cs (Unity)
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
| 4 — Fighter's Rhythm | State machine + autonomous step loop + kicks | ⬜ |
| 5 — Polish | Timing, anticipation, flow refinement | ⬜ |

## Current Status

**Milestone 3 complete.** Motion blending shipped:
- `MotionPlayer` captures live pose on `Load()` and LERPs to new clip over `blendDuration` (default 0.25s, tweakable in Inspector)
- Blend runs "underneath" — new clip advances during blend so no jump when it completes
- First load (startup) bypasses blend; `Stop()` also clears blend state
- `JointMat` (URP Lit) and `BoneMat` (URP Unlit) material assets created and wired into scene
- `AndroidBridge` scene GameObject renamed to `"AndroidBridge"` (required by `UnitySendMessage`)
- Android test button sends `test_motion.json` (same file as Unity `sampleClip`) to verify blending
- Exit/back/✕ all navigate back to `LauncherActivity` via `FLAG_ACTIVITY_CLEAR_TOP` (prevents Unity process kill from closing the whole app)

**Next:** Milestone 4 — Fighter's Rhythm (state machine + autonomous step loop + kicks).

## Non-Goals

- Real-time pose tracking (MediaPipe is offline only)
- Procedural animation or IK
- Unity Animator / Mecanim
- Scoring or reaction timing
- Manual joint manipulation

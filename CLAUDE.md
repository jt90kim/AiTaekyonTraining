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
| 3 — Smooth Moves | Motion blending (0.2–0.4s LERP between clips) | ⬜ |
| 4 — Fighter's Rhythm | State machine + autonomous step loop + kicks | ⬜ |
| 5 — Polish | Timing, anticipation, flow refinement | ⬜ |

## Current Status

**Milestone 2 complete.** Android app (Taekyon Trainer) builds and runs:
- `LauncherActivity` — splash screen + training setup UI (duration picker + flat move list)
- `MainActivity` — extends `UnityPlayerGameActivity`; receives selected moves + duration via Intent, overlays Compose controls on Unity
- Unity as a Library (UaaL) integrated via path reference (`unity-export/unityLibrary`) — no manual copy needed after re-export
- App theme: `Theme.AppCompat.NoActionBar` with dark/orange Taekyon brand palette
- Move list populated at runtime from `assets/motions/*.json`

**Next:** Milestone 3 — motion blending (0.2–0.4s LERP between clips) in Unity.

## Non-Goals

- Real-time pose tracking (MediaPipe is offline only)
- Procedural animation or IK
- Unity Animator / Mecanim
- Scoring or reaction timing
- Manual joint manipulation

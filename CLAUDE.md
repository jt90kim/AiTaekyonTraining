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
| 4 — Fighter's Rhythm | State machine + autonomous step loop + kicks | ✅ Done |
| 5 — Polish | Timing, anticipation, flow refinement | ✅ Done |
| 6 — Real Kicks | Capture kick clips via MediaPipe; wire into state machine | 🔜 Next |

## Current Status

**Milestone 5 complete.** Polish shipped:
- `MotionPlayer`: blend easing upgraded from linear LERP to `Mathf.SmoothStep`; `Load()` accepts optional `blendOverride` so each transition uses its own duration
- `MotionStateMachine`: new `Anticipating` state pauses ~0.3s before each step (telegraphs the move); per-transition blend durations wired in — step entry 0.15s (snappy), return-to-neutral 0.30s (settle), kick entry 0.10s (explosive), kick recovery 0.40s (slow); all values tweakable in Inspector

**Next:** Milestone 6 — capture real kick clips via MediaPipe offline, export as JSON, assign to `leftKickClip` / `rightKickClip` in the Inspector.

## Non-Goals

- Real-time pose tracking (MediaPipe is offline only)
- Procedural animation or IK
- Unity Animator / Mecanim
- Scoring or reaction timing
- Manual joint manipulation

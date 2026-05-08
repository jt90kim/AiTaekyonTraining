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
| 6 — Real Kicks | Capture kick clips via MediaPipe; wire into data-driven state machine | ✅ Done |
| 7 — Visual Polish | Capsule renderer, color coding, floor grid, perspective camera | ✅ Done |

## Current Status

**All milestones complete.** The opponent runs autonomously, performs kicks continuously, and the scene renders cleanly with color-coded limbs and a perspective floor grid for depth reference.

### Scene configuration (SampleScene.unity)

| Setting | Value |
|---|---|
| Camera | Perspective, FOV 50, position (0, 1.5, -3), 5° downward tilt |
| Skeleton root Y rotation | ~190° (faces camera) |
| Move probability | 95% |
| Idle duration | 0 (no pause between actions) |
| Transition speed | 3× |
| Move (kick) speed | 2.5× |

### Visual system (DebugSkeletonRenderer.cs)

- **Arms:** orange cylinders
- **Left leg:** blue cylinders
- **Right leg:** red cylinders
- **Torso:** gray cylinders
- **Head:** yellow sphere
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

### Move type naming convention
```
{move_type}_{stance}_{leg_role}_{height}.json
  stance   = left | right   (which foot is FORWARD)
  leg_role = front | rear   (which leg executes, relative to stance)
  height   = low | high     (target height of the kick)
```

**Next:** Android integration testing — verify `SetEnabledMoves` bridge from Android UI correctly filters move variants in Unity.

## Non-Goals

- Real-time pose tracking (MediaPipe is offline only)
- Procedural animation or IK
- Unity Animator / Mecanim
- Scoring or reaction timing
- Manual joint manipulation

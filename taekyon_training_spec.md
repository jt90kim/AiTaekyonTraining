# Taekyon Training App — System Specification

## 🎯 Project Goal

Build a Unity-based Taekyon training application where:

- An AI opponent performs pre-authored Taekyon motions
- The user observes and reacts to movements
- AI opponent does not keep track of user movements. This may be a requirement in the far future but no in the forseeable future
- Movements are realistic and reflect proper 결련택견 style


## Workspace Structure

```
claude/
├── TaekyonClaude/       # Android project (UI + motion library)
├── Unity/     # Unity project (primary codebase)
└── unity-export/             # ⛔ AUTO-GENERATED — never read or modify
```

## ⛔ Off-Limits Directories (never read or modify)

| Path | Reason |
|------|--------|
| `unity-export/` | Unity build export, 100% auto-generated |
| `TaekyonClaude/build/` | Android build output |
| `TaekyonClaude/.idea/` | IDE metadata |
| `TaekyonClaude/.git/` | Git internals |
| `TaekyonClaude/.gradle/` | Gradle cache |
| `TaekyonClaude/gradle/` | Gradle wrapper |


---

## ⚠️ Critical Constraint

All motions MUST be generated using MediaPipe (offline), then exported as JSON.

> Unity does NOT generate motion.  
> Unity ONLY plays back motion data.


- **Scenes:** `Unity/UnityTaekyon/Assets/Scenes/`
- **Scripts:** `Unity/UnityTaekyon/Assets/Scripts/`

### Android
- `TaekyonClaude/` (excluding off-limits folders above)

---

## ❌ Non-Goals

- No real-time pose tracking (MediaPipe is offline only)
- No procedural animation (no synthetic stepping logic)
- No inverse kinematics (IK) or physics-based animation
- No Unity Animator / Mecanim usage
- No scoring or reaction timing systems
- No manual joint manipulation to simulate motion

---

## 🔮 Future Goals (Out of Scope for Now)

- User-provided motion capture
- Expanded motion library (more techniques)
- Reaction timing / feedback system
- Advanced blending between motions
- Potential IK refinement (if needed later)

---

# 🧠 Architecture

## Core Principle

All movement must come from MediaPipe-captured motion data.

---

## Motion Data Pipeline

Real Taekyon Movement  
→ MediaPipe (offline capture)  
→ Process into joint data  
→ Export as JSON  
→ Unity loads JSON → plays motion  

---

## Motion Format

MotionClip  
  fps: int  
  frames: MotionFrame[]  

MotionFrame  
  joints: Dictionary<string, Vector3>  

---

## Playback Pipeline

Android (stores JSON motion library)  
→ AndroidBridge.ReceiveMotionMessage(json)  
→ MotionLoader (parse JSON)  
→ MotionPlayer (frame playback + interpolation)  
→ SkeletonMapper (apply transforms)  
→ DebugSkeletonRenderer (render skeleton)  

---

# 🧩 State-Based Motion System

## States

Neutral  
LeftForward  
RightForward  
Transition  

---

## Step System (MediaPipe-Based)

Steps are NOT procedural.

They are captured motion clips:

- Neutral → LeftForward  
- LeftForward → Neutral  
- Neutral → RightForward  
- RightForward → Neutral  

---

## Step Loop

Neutral  
→ LeftForward  
→ Neutral  
→ RightForward  
→ Neutral  
→ repeat  

---

## Kick System

Kicks are also MediaPipe-generated clips.

### Constraints

- Left Kick → LeftForward  
- Right Kick → RightForward  

---

## Kick Behavior

- Kicks occur randomly when in a valid state  
- After a kick:
  - Return to the SAME stance (not neutral)  

---

# 🎬 Motion Blending

- Blend from current pose → first frame of next motion  
- Duration: ~0.2–0.4 seconds  
- Simple interpolation with easing  
- No complex animation graph  

---

# ⏱ Timing

- Small delay before motion starts (anticipation)  
- Controlled rhythm between motions  
- No advanced timing system  

---

# 🦴 Skeleton System

- Transform-based joint system  
- Skeleton is rendered via DebugSkeletonRenderer  
- Requires a valid pose to be visible  
- A default pose must be applied at startup  

---

# ⚠️ Critical Implementation Rules

- Do NOT procedurally generate movement  
- Do NOT manipulate individual joints to simulate motion  
- Do NOT break bone relationships (no stretching)  
- Always use motion clips as the source of truth  

---

# 🧰 Technologies

## Engine
- Unity (URP)  
- C#  

## Motion System
- MotionClip  
- MotionFrame  
- MotionLoader  
- MotionPlayer  

## Skeleton
- SkeletonMapper  
- Transform-based mapping  

## Integration
- AndroidBridge (receives JSON)  

## Motion Capture (CRITICAL)
- MediaPipe (offline only)  
- Generates ALL motion data (steps + kicks)  

## Rendering
- DebugSkeletonRenderer  
- Joint spheres + bone lines  

---

# 🔥 Design Philosophy

## 1. Data-Driven Motion
All movement comes from captured data, not simulation.

## 2. State-Driven Logic
Behavior is controlled by stance states.

## 3. Minimal Complexity
Avoid IK, physics, and complex animation systems.

## 4. Style Fidelity (결련택견)
- Whole-body motion  
- Natural flow  
- No artificial joint manipulation  

---

# 🚀 Tasks for Implementation (for Claude)

## Phase 1 — Core Motion System
- Implement MotionClip and MotionFrame data structures
- Implement MotionLoader to parse JSON into MotionClip
- Implement MotionPlayer:
  - Frame stepping
  - Interpolation between frames
  - Playback control (start, stop, pause)

---

## Phase 2 — Skeleton System
- Implement SkeletonMapper:
  - Map joint names → Unity transforms
  - Apply MotionFrame to transforms
- Implement default pose initialization

---

## Phase 3 — Rendering
- Implement DebugSkeletonRenderer:
  - Draw joints (spheres)
  - Draw bones (lines)
  - Ensure visibility only when valid pose exists

---

## Phase 4 — Android Integration
- Implement AndroidBridge:
  - Receive JSON motion
  - Pass to MotionLoader and MotionPlayer
  - Control playback timing

---

## Phase 5 — Motion Blending
- Implement blending system:
  - Capture current pose
  - Blend to first frame of next clip
  - Smooth transition (0.2–0.4s)

---

## Phase 6 — State Machine
- Implement stance states:
  - Neutral, LeftForward, RightForward, Transition
- Implement step loop using motion clips
- Ensure correct state transitions

---

## Phase 7 — Kick Integration
- Inject kicks based on state:
  - Left kick only in LeftForward
  - Right kick only in RightForward
- Ensure return to same stance after kick

---

## Phase 8 — Refinement
- Tune timing and delays
- Improve motion flow
- Ensure no snapping or unnatural transitions

---

# ✅ Final Summary

This system is a state-driven motion playback engine in Unity.

All movements are captured via MediaPipe offline, converted to JSON,
and played back using a custom motion system.

No procedural animation is used.

State transitions control which motion clips are played and when kicks occur.

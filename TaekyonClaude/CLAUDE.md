# CLAUDE.md — TaekyonClaude (Android)

## Responsibility

This Android project is the **motion library host and bridge** to Unity.

- Stores pre-captured JSON motion clips (steps + kicks)
- Sends JSON strings to the Unity layer via AndroidBridge
- Provides any UI needed to control playback (start, stop, select technique)

## Working Directories

| Path | Contents |
|------|----------|
| `app/src/main/java/` | Application source code |
| `app/src/main/assets/` | JSON motion clip files |
| `app/src/main/res/` | Android layouts and resources |

## ⛔ Off-Limits

| Path | Reason |
|------|--------|
| `build/` | Build output |
| `.idea/` | IDE metadata |
| `.git/` | Git internals |
| `.gradle/` | Gradle cache |
| `gradle/` | Gradle wrapper |

## Motion JSON Format

```json
{
  "fps": 30,
  "frames": [
    {
      "joints": {
        "left_hip": [0.1, 0.9, 0.0],
        "right_hip": [-0.1, 0.9, 0.0]
      }
    }
  ]
}
```

Each joint value is a `[x, y, z]` Vector3.

## Bridge Protocol

The Android side calls Unity via `UnitySendMessage`:

```java
UnityPlayer.UnitySendMessage("AndroidBridge", "ReceiveMotionMessage", jsonString);
```

The target GameObject in Unity is `AndroidBridge`, method is `ReceiveMotionMessage`.

## Motion Clip Naming Convention

Use descriptive snake_case filenames stored in `assets/motions/`:

```
neutral_to_left.json
left_to_neutral.json
neutral_to_right.json
right_to_neutral.json
left_kick.json
right_kick.json
```

## Current Status

Empty project — no source code yet. Phase 1 (Unity core) comes first.

## Non-Goals

- No user pose detection
- No real-time MediaPipe processing (all JSON is pre-captured offline)
- No scoring logic

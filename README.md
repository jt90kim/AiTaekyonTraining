# Taekyun Trainer

A mobile training app for 결련택견 (Gyeollyon Taekyun), a traditional Korean martial art. An AI-controlled opponent performs pre-authored techniques in real time — the user observes and reacts.

No scoring. No pose tracking. Just rhythm.

---

## What it is

The app pairs an Android shell with a Unity 6 scene. The Unity side plays back motion-captured kick and step sequences on a procedural mannequin. The Android side controls session setup, timing, and UI.

The opponent moves autonomously: it cycles through stances, throws roundhouse and splint kicks with realistic timing, and transitions fluidly between clips using frame interpolation and motion blending.

---

## Architecture

```
Android (TaekyonClaude)          Unity (UnityTaekyon)
────────────────────────         ──────────────────────────────
Session setup UI                 MotionStateMachine
  duration picker                  stance loop (neutral / left / right)
  technique selector               transition clips
                                   kick variants (roundhouse, splint)
         │  UnitySendMessage
         │  SetEnabledMoves(csv) ──▶ AndroidBridge.cs
         │  SetPaused(bool)      ──▶   Time.timeScale 0 / 1
         │
         ◀── onUnitySceneReady()      MotionPlayer.cs
                                        frame interpolation
Training overlay                        motion blending (LERP)
  3-second countdown               SkeletonMapper.cs
  live timer + kick counter        MannequinRenderer.cs
  pause / resume pill                procedural mesh per frame
  exit confirmation dialog
  session complete card
```

---

## Motion library

All motion data is captured offline via MediaPipe and exported as JSON. Unity never generates or synthesizes movement.

| Clip | Type |
|---|---|
| `neutral_idle.json` | stance loop |
| `left_forward_idle.json` | stance loop |
| `right_forward_idle.json` | stance loop |
| `neutral_to_left.json` · `left_to_neutral.json` | transitions |
| `neutral_to_right.json` · `right_to_neutral.json` | transitions |
| `roundhouse_{left\|right}_{front\|rear}_low.json` | kick variants |
| `roundhouse_{left\|right}_{front\|rear}_high.json` | kick variants |
| `splint_{left\|right}_{front\|rear}_{low\|high}.json` | kick variants |

Clip naming: `{move_type}_{stance}_{leg_role}_{height}.json`

---

## Project structure

```
claude/
├── TaekyonClaude/        Android app (Jetpack Compose, Kotlin)
│   ├── app/src/main/java/com/taekyun/flow/
│   │   ├── LauncherActivity.kt   splash + session setup
│   │   ├── MainActivity.kt       training overlay (Unity host)
│   │   └── MotionLibrary.kt      technique catalog + i18n
│   └── app/src/main/assets/      (motion JSONs served to Unity)
├── Unity/UnityTaekyon/   Unity 6 project
│   └── Assets/Scripts/
│       ├── AndroidBridge.cs
│       ├── MotionStateMachine.cs
│       ├── MotionPlayer.cs
│       ├── MotionLoader.cs
│       ├── SkeletonMapper.cs
│       └── MannequinRenderer.cs
├── tools/
│   ├── extract_motion.py   MediaPipe → JSON extractor (offline)
│   └── capture_guide.md
└── unity-export/          ⛔ auto-generated, do not edit
```

---

## Android app

- **Min SDK:** 29 (Android 10)
- **Package:** `com.taekyun.flow`
- **Theme:** dark / light adaptive (`isSystemInDarkTheme()`)
- **Localization:** English + Korean (`values/` + `values-ko/`)
- **Screen wake:** `FLAG_KEEP_SCREEN_ON` + Unity `Screen.sleepTimeout = NeverSleep`

### Session flow

1. Splash screen → "Begin training" (1.5 s fade-in delay)
2. Setup screen — pick duration (30 s – 5 m) and techniques
3. Unity loads → 3-second countdown → training starts
4. Training overlay: timer, kick counter, pause pill, exit confirmation
5. Time up → session complete card (duration, kicks, techniques used)

---

## Building

### Android

```bash
cd TaekyonClaude
./gradlew :app:assembleDebug
```

Requires Unity export in `unity-export/` — build Unity first via *File → Build Settings → Export Project*.

### Unity

Open `Unity/UnityTaekyon` in Unity 6. Scene: `Assets/Scenes/SampleScene.unity`. Build target: Android, ARM64.

### Motion capture (offline)

```bash
pip install mediapipe opencv-python
python tools/extract_motion.py --input clip.mp4 --output motion.json
```

See `tools/capture_guide.md` for filming tips and script usage.

---

## Non-goals

- Real-time pose tracking or user detection
- Procedural / IK animation
- Unity Animator / Mecanim
- Scoring or reaction timing measurement

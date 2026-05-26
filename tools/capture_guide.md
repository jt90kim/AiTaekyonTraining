# Motion Capture Guide — Taekyon Training App

## File Naming Convention

All motion clip filenames follow one of three patterns:

```
Stance idle:   {stance}_idle.json
Transition:    {from_stance}_to_{to_stance}.json
Move variant:  {move_type}_{stance}_{leg_role}_{height}.json
```

### Dimension definitions

| Dimension | Values | Meaning |
|---|---|---|
| `stance` | `left`, `right`, `neutral` | Which foot is **forward** in the stance |
| `leg_role` | `front`, `rear` | Which leg **executes** the technique, relative to the stance |
| `height` | `low`, `high` | Target height of the kick |

**Rule:** `left` / `right` always describes the **stance** (which foot is forward). It never describes which leg kicks. The kicking leg is always expressed as `front` or `rear`.

### Examples

| Filename | Plain English |
|---|---|
| `roundhouse_left_front_low.json` | Low roundhouse — left foot forward stance — front (left) leg kicks |
| `roundhouse_left_rear_low.json` | Low roundhouse — left foot forward stance — rear (right) leg kicks |
| `roundhouse_right_front_low.json` | Low roundhouse — right foot forward stance — front (right) leg kicks |
| `roundhouse_right_rear_low.json` | Low roundhouse — right foot forward stance — rear (left) leg kicks |
| `roundhouse_left_front_high.json` | High roundhouse — left foot forward stance — front (left) leg kicks |

---

## Camera & Environment Setup

- **Distance:** 2–3 meters from the camera
- **Framing:** Full body visible from head to feet in every frame
- **Camera height:** Approximately hip height (not floor level, not overhead)
- **Angle:** **3/4 angle — ~30–45° off front-on, from the fighter's right side.** Stand facing slightly away from camera so your right shoulder is closer to it. Do NOT film dead front-on.
- **Same angle for all clips:** Lock the tripod and do not move it between clips. All clips must be filmed from the same angle. Inconsistent angles produce mismatched coordinate frames that break blending in Unity.
- **Facing correction is automatic:** `extract_motion.py` measures the average shoulder vector and rotates each clip to face Unity's +X axis. You do not need to measure or input the exact angle — just be consistent between clips.
- **Background:** Plain wall or solid-color surface preferred
- **Lighting:** Bright and even; avoid strong backlighting
- **Clothing:** Fitted clothes — loose or baggy fabric reduces MediaPipe accuracy

### Why 3/4 angle?

Taekyon involves heavy forward and backward movement — steps and kick extensions move primarily along the depth axis (z). MediaPipe estimates depth from a single camera using learned body proportions, and this estimate degrades when the motion goes directly toward or away from the lens. Filming at a 3/4 angle turns that depth movement into a visible diagonal in the image, giving MediaPipe a much better signal. Front-on captures x and y well but depth poorly; 3/4 balances all three axes.

---

## Clips to Record

Record each clip as a separate short video. Do 3–5 takes per clip and keep the cleanest one.

### Stance idles (3 clips)

These are looping idle animations. Move naturally — do not freeze. The loop should feel like a continuous, comfortable hold.

| Output filename | What to perform | Duration |
|---|---|---|
| `neutral_idle.json` | Stand in natural Taekyon resting stance. Gentle sway or weight shift. | ~3–4 s |
| `left_forward_idle.json` | Left foot forward, slight knee bend, natural arm position. Subtle movement. | ~3–4 s |
| `right_forward_idle.json` | Mirror of above — right foot forward. | ~3–4 s |

### Stance transitions (4 clips)

These connect stances. Start and end at the same pose as the idle clips they connect — this ensures seamless blending.

| Output filename | What to perform | Duration |
|---|---|---|
| `neutral_to_left.json` | From neutral: step the left foot forward into left forward stance. Land and hold 1 beat. | ~1.5 s |
| `left_to_neutral.json` | From left forward stance: bring left foot back to neutral. | ~1.5 s |
| `neutral_to_right.json` | From neutral: step the right foot forward into right forward stance. Land and hold 1 beat. | ~1.5 s |
| `right_to_neutral.json` | From right forward stance: bring right foot back to neutral. | ~1.5 s |

### Low roundhouse kick variants (4 clips)

Each kick clip starts from the matching forward stance. The motion is: raise knee → extend kick at low height → retract → hold briefly at the end pose.

| Output filename | Starting stance | Kicking leg | What to perform |
|---|---|---|---|
| `roundhouse_left_front_low.json` | Left foot forward | Front (left) leg | Raise left knee, extend kick low, retract, hold |
| `roundhouse_left_rear_low.json` | Left foot forward | Rear (right) leg | Raise right knee, extend kick low, retract, hold |
| `roundhouse_right_front_low.json` | Right foot forward | Front (right) leg | Raise right knee, extend kick low, retract, hold |
| `roundhouse_right_rear_low.json` | Right foot forward | Rear (left) leg | Raise left knee, extend kick low, retract, hold |

Duration per kick clip: ~1.5–2 s

### High roundhouse kick variants (4 clips — capture later)

Same as above but kick extends to chest/head height.

| Output filename | Starting stance | Kicking leg |
|---|---|---|
| `roundhouse_left_front_high.json` | Left foot forward | Front (left) leg |
| `roundhouse_left_rear_high.json` | Left foot forward | Rear (right) leg |
| `roundhouse_right_front_high.json` | Right foot forward | Front (right) leg |
| `roundhouse_right_rear_high.json` | Right foot forward | Rear (left) leg |

---

### Low splint kick variants / 내차기 (4 clips)

The splint kick sweeps the shin inward across the centerline in a rising arc. The motion is: shift weight → raise knee inward → extend shin across → retract → hold end pose.

| Output filename | Starting stance | Kicking leg | What to perform |
|---|---|---|---|
| `splint_left_front_low.json` | Left foot forward | Front (left) leg | Raise left knee inward, sweep shin across low, retract, hold |
| `splint_left_rear_low.json` | Left foot forward | Rear (right) leg | Raise right knee inward, sweep shin across low, retract, hold |
| `splint_right_front_low.json` | Right foot forward | Front (right) leg | Raise right knee inward, sweep shin across low, retract, hold |
| `splint_right_rear_low.json` | Right foot forward | Rear (left) leg | Raise left knee inward, sweep shin across low, retract, hold |

Duration per clip: ~1.5–2 s

---

## Extracting a Clip

### Install dependencies (one time)

```
pip install mediapipe opencv-python
```

### Download the pose model (one time)

MediaPipe 0.10+ requires a separate model file. Download it once into the `tools/` folder:

```bash
curl -L -o tools/pose_landmarker_heavy.task \
  https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_heavy/float16/1/pose_landmarker_heavy.task
```

The file is ~29 MB and only needs to be downloaded once. The script will print an error with this exact command if the file is missing.

### Run the script

```
python tools/extract_motion.py <input_video> <output.json>
```

### Commands — stance idles and transitions

```bash
python tools/extract_motion.py neutral_idle.mp4          neutral_idle.json
python tools/extract_motion.py left_forward_idle.mp4     left_forward_idle.json
python tools/extract_motion.py right_forward_idle.mp4    right_forward_idle.json

python tools/extract_motion.py neutral_to_left.mp4       neutral_to_left.json
python tools/extract_motion.py left_to_neutral.mp4       left_to_neutral.json
python tools/extract_motion.py neutral_to_right.mp4      neutral_to_right.json
python tools/extract_motion.py right_to_neutral.mp4      right_to_neutral.json
```

### Commands — low roundhouse kicks

```bash
python tools/extract_motion.py roundhouse_left_front_low.mp4    roundhouse_left_front_low.json
python tools/extract_motion.py roundhouse_left_rear_low.mp4     roundhouse_left_rear_low.json
python tools/extract_motion.py roundhouse_right_front_low.mp4   roundhouse_right_front_low.json
python tools/extract_motion.py roundhouse_right_rear_low.mp4    roundhouse_right_rear_low.json
```

### Commands — high roundhouse kicks (when ready)

```bash
python tools/extract_motion.py roundhouse_left_front_high.mp4   roundhouse_left_front_high.json
python tools/extract_motion.py roundhouse_left_rear_high.mp4    roundhouse_left_rear_high.json
python tools/extract_motion.py roundhouse_right_front_high.mp4  roundhouse_right_front_high.json
python tools/extract_motion.py roundhouse_right_rear_high.mp4   roundhouse_right_rear_high.json
```

### Commands — low splint kicks / 내차기

```bash
python tools/extract_motion.py splint_left_front_low.mp4    splint_left_front_low.json
python tools/extract_motion.py splint_left_rear_low.mp4     splint_left_rear_low.json
python tools/extract_motion.py splint_right_front_low.mp4   splint_right_front_low.json
python tools/extract_motion.py splint_right_rear_low.mp4    splint_right_rear_low.json
```

### Sanity-checking the output

After each run the script prints:

```
Extracted 22 frames (0 missed) → roundhouse_left_front_low.json
  y offset applied:   +0.923m  (grounded ankles to y=0.05)
  facing correction:  +34.7 deg around Y  (aligned shoulders to +X)
  Nose y range:  1.512 – 1.731  (expect ~1.4-1.8 for upright stance)
  Ankle y range: 0.031 – 0.684  (expect ~0.05 standing, higher during kick)
```

- **Nose y** should be in the `1.4–1.8` range for standing poses
- **Ankle y** should rise noticeably above zero during kick clips (the kicking foot lifts)
- For low kicks, ankle y rise will be modest; for high kicks it will be much larger
- **Facing correction** rotates the whole clip so the skeleton faces Unity's camera — a non-zero value is normal and expected when filming at a 3/4 angle
- If nose y is negative or near zero, the skeleton may be flipped — report it before continuing

---

## Where to Place the Output Files

Copy each `.json` file to **both** locations:

```
Unity/UnityTaekyon/Assets/SampleMotions/   ← Unity TextAsset references
TaekyonClaude/app/src/main/assets/motions/ ← Android asset mirror
```

After placing files, assign them in the Unity Inspector under `MotionStateMachine` (Stances, Transitions, and MoveVariants arrays).

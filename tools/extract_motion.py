"""
extract_motion.py — MediaPipe Pose Landmarker -> Taekyon JSON clip extractor

Requires a model file in the same directory as this script.
Download it once (see capture_guide.md for instructions).

Usage:
    python tools/extract_motion.py <input_video> <output.json>

Output format:
    { "fps": 8, "frames": [ { "joints": { "nose": [x,y,z], ... } } ] }

Axis convention (matching Unity clips):
    unity_x = -mediapipe_x   (camera-right  -> subject-right)
    unity_y = -mediapipe_y   (MP y-down      -> Unity y-up)
    unity_z = -mediapipe_z   (toward-camera  -> forward)
"""

import cv2
import json
import math
import os
import sys

import mediapipe as mp
from mediapipe.tasks import python as mp_python
from mediapipe.tasks.python import vision as mp_vision

TARGET_FPS    = 15
MODEL_FILENAME = "pose_landmarker_heavy.task"

# MediaPipe BlazePose landmark indices -> joint names used by Unity
JOINTS = {
    0:  "nose",
    7:  "left_ear",
    8:  "right_ear",
    11: "left_shoulder",
    12: "right_shoulder",
    13: "left_elbow",
    14: "right_elbow",
    15: "left_wrist",
    16: "right_wrist",
    23: "left_hip",
    24: "right_hip",
    25: "left_knee",
    26: "right_knee",
    27: "left_ankle",
    28: "right_ankle",
    29: "left_heel",
    30: "right_heel",
    31: "left_foot_index",
    32: "right_foot_index",
}


def _rot_y(pos: list, cos_a: float, sin_a: float) -> list:
    x, y, z = pos
    return [round(cos_a * x - sin_a * z, 4), y, round(sin_a * x + cos_a * z, 4)]


def _rot_z(pos: list, cos_a: float, sin_a: float) -> list:
    x, y, z = pos
    return [round(cos_a * x - sin_a * y, 4), round(sin_a * x + cos_a * y, 4), z]


def apply_facing_correction(frames_out: list) -> float:
    """Rotate all frames so the skeleton faces straight forward (+X shoulder vector).

    MediaPipe world-landmark Z points toward the filming camera, not Unity's camera.
    When filmed at a 3/4 angle the skeleton appears tilted in Unity.  This corrects
    for that by measuring the average shoulder vector and rotating to align it with +X.

    Returns the correction angle in degrees (for the sanity-check printout).
    """
    avg_dx = sum(
        fr["joints"]["left_shoulder"][0] - fr["joints"]["right_shoulder"][0]
        for fr in frames_out
    ) / len(frames_out)
    avg_dz = sum(
        fr["joints"]["left_shoulder"][2] - fr["joints"]["right_shoulder"][2]
        for fr in frames_out
    ) / len(frames_out)

    correction = -math.atan2(avg_dz, avg_dx)   # rotate shoulder vector onto +X axis
    # Clamp to ±90° — a ~180° correction would flip the body's facing direction
    if correction > math.pi / 2:
        correction -= math.pi
    elif correction < -math.pi / 2:
        correction += math.pi
    cos_c = math.cos(correction)
    sin_c = math.sin(correction)

    for fr in frames_out:
        for name in fr["joints"]:
            fr["joints"][name] = _rot_y(fr["joints"][name], cos_c, sin_c)

    return math.degrees(correction)


def apply_shoulder_level(frames_out: list) -> float:
    """Rotate all frames around Z (roll) to level the shoulders.

    When filmed at an angle, MediaPipe's depth estimate creates a small
    systematic Y difference between the two shoulders. This measures the
    average shoulder tilt and rolls the whole skeleton to cancel it.

    In our coordinate system left_shoulder.x < right_shoulder.x (the
    x axis is flipped from camera-right to subject-right), so the shoulder
    vector (left - right) points toward -X. We negate avg_dx before passing
    to atan2 so the angle is measured relative to -X, giving a small
    deviation angle rather than one near ±180°.

    Returns the correction angle in degrees.
    """
    avg_dy = sum(
        fr["joints"]["left_shoulder"][1] - fr["joints"]["right_shoulder"][1]
        for fr in frames_out
    ) / len(frames_out)
    avg_dx = sum(
        fr["joints"]["left_shoulder"][0] - fr["joints"]["right_shoulder"][0]
        for fr in frames_out
    ) / len(frames_out)

    # Negate avg_dx: left.x - right.x is negative in our coord (left is at -x).
    # atan2(dy, -avg_dx) measures tilt relative to the expected -X shoulder direction.
    # correction = tilt (not -tilt) because positive roll lowers left shoulder (at -x).
    tilt = math.atan2(avg_dy, -avg_dx)
    correction = tilt
    cos_c = math.cos(correction)
    sin_c = math.sin(correction)

    for fr in frames_out:
        for name in fr["joints"]:
            fr["joints"][name] = _rot_z(fr["joints"][name], cos_c, sin_c)

    return math.degrees(tilt)


def find_model() -> str:
    script_dir = os.path.dirname(os.path.abspath(__file__))
    model_path = os.path.join(script_dir, MODEL_FILENAME)
    if not os.path.exists(model_path):
        print(f"ERROR: model file not found at '{model_path}'")
        print()
        print("Download it once with (run from the repo root):")
        print(f"  curl -L -o tools/{MODEL_FILENAME} \\")
        print( "    https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_heavy/float16/1/pose_landmarker_heavy.task")
        sys.exit(1)
    return model_path


def extract(video_path: str, output_path: str) -> None:
    model_path = find_model()

    base_options = mp_python.BaseOptions(model_asset_path=model_path)
    options = mp_vision.PoseLandmarkerOptions(
        base_options=base_options,
        running_mode=mp_vision.RunningMode.VIDEO,
        num_poses=1,
        min_pose_detection_confidence=0.5,
        min_pose_presence_confidence=0.5,
        min_tracking_confidence=0.5,
        output_segmentation_masks=False,
    )

    cap = cv2.VideoCapture(video_path)
    if not cap.isOpened():
        print(f"ERROR: could not open video '{video_path}'")
        sys.exit(1)

    source_fps = cap.get(cv2.CAP_PROP_FPS) or 30.0
    frame_step = max(1, round(source_fps / TARGET_FPS))
    print(f"Source FPS: {source_fps:.1f}  ->  sampling every {frame_step} frames  ->  ~{TARGET_FPS} fps output")

    frames_out   = []
    source_frame_idx = 0
    missed       = 0

    with mp_vision.PoseLandmarker.create_from_options(options) as landmarker:
        while True:
            ret, frame = cap.read()
            if not ret:
                break

            # Timestamp must be monotonically increasing for VIDEO mode
            timestamp_ms = int(source_frame_idx * 1000 / source_fps)

            if source_frame_idx % frame_step == 0:
                rgb      = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
                mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb)
                result   = landmarker.detect_for_video(mp_image, timestamp_ms)

                if result.pose_world_landmarks:
                    lm     = result.pose_world_landmarks[0]
                    joints = {}
                    for idx, name in JOINTS.items():
                        p = lm[idx]
                        joints[name] = [
                            round(-p.x, 4),
                            round(-p.y, 4),
                            round(-p.z, 4),
                        ]
                    frames_out.append({"joints": joints})
                else:
                    missed += 1
                    print(f"  WARNING: no pose at source frame {source_frame_idx} (skipped)")

            source_frame_idx += 1

    cap.release()

    if not frames_out:
        print("ERROR: no frames extracted — check that a full-body pose is visible throughout the video.")
        sys.exit(1)

    # 1. Face the skeleton forward (Y rotation — corrects 3/4 filming angle in XZ).
    rotation_deg = apply_facing_correction(frames_out)

    # 2. Level the shoulders (Z roll — corrects MediaPipe depth bias in Y).
    shoulder_tilt_deg = apply_shoulder_level(frames_out)

    # 3. Ground last, after rotations, so the lowest ankle is at y=0.05 regardless
    #    of how the roll correction shifted joint positions.
    min_ankle_y = min(
        min(fr["joints"]["left_ankle"][1], fr["joints"]["right_ankle"][1])
        for fr in frames_out
    )
    y_offset = 0.05 - min_ankle_y
    for fr in frames_out:
        for name in fr["joints"]:
            fr["joints"][name][1] = round(fr["joints"][name][1] + y_offset, 4)

    clip = {"fps": TARGET_FPS, "frames": frames_out}
    with open(output_path, "w") as f:
        json.dump(clip, f, indent=2)

    # Sanity-check printout
    nose_ys  = [fr["joints"]["nose"][1] for fr in frames_out]
    ankle_ys = [
        min(fr["joints"]["left_ankle"][1], fr["joints"]["right_ankle"][1])
        for fr in frames_out
    ]
    print(f"\nExtracted {len(frames_out)} frames  ({missed} missed)  ->  {output_path}")
    print(f"  y offset applied:   {y_offset:+.3f}m  (grounded ankles to y=0.05)")
    print(f"  facing correction:  {rotation_deg:+.1f} deg around Y  (aligned shoulders to +X)")
    print(f"  shoulder level:     {shoulder_tilt_deg:+.1f} deg roll corrected  (levelled shoulders)")
    print(f"  Nose y range:  {min(nose_ys):.3f} to {max(nose_ys):.3f}  (expect ~1.4-1.8 for upright stance)")
    print(f"  Ankle y range: {min(ankle_ys):.3f} to {max(ankle_ys):.3f}  (expect ~0.05 standing, higher during kick)")
    if max(nose_ys) < 0.8:
        print("  WARNING: nose y seems low -- skeleton may be upside-down, report to Claude before continuing.")
    if abs(min(nose_ys)) < 0.1 and abs(max(nose_ys)) < 0.1:
        print("  WARNING: all y values near zero -- world landmarks may not be returning data.")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python extract_motion.py <input_video> <output.json>")
        sys.exit(1)
    extract(sys.argv[1], sys.argv[2])

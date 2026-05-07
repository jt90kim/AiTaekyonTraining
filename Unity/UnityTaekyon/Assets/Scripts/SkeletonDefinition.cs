using UnityEngine;

public static class SkeletonDefinition
{
    public static readonly string[] JointNames =
    {
        "nose",
        "left_ear", "right_ear",
        "left_shoulder", "right_shoulder",
        "left_elbow",   "right_elbow",
        "left_wrist",   "right_wrist",
        "left_hip",     "right_hip",
        "left_knee",    "right_knee",
        "left_ankle",   "right_ankle",
        "left_heel",    "right_heel",
        "left_foot_index", "right_foot_index"
    };

    // Each entry is (fromJoint, toJoint)
    public static readonly (string, string)[] Bones =
    {
        // Head
        ("left_ear",  "nose"),
        ("right_ear", "nose"),

        // Torso
        ("left_shoulder",  "right_shoulder"),
        ("left_shoulder",  "left_hip"),
        ("right_shoulder", "right_hip"),
        ("left_hip",       "right_hip"),

        // Left arm
        ("left_shoulder", "left_elbow"),
        ("left_elbow",    "left_wrist"),

        // Right arm
        ("right_shoulder", "right_elbow"),
        ("right_elbow",    "right_wrist"),

        // Left leg
        ("left_hip",   "left_knee"),
        ("left_knee",  "left_ankle"),
        ("left_ankle", "left_heel"),
        ("left_ankle", "left_foot_index"),

        // Right leg
        ("right_hip",   "right_knee"),
        ("right_knee",  "right_ankle"),
        ("right_ankle", "right_heel"),
        ("right_ankle", "right_foot_index"),
    };

    public static Color GetJointColor(string jointName)
    {
        if (jointName.StartsWith("left_"))  return new Color(0.2f, 0.6f, 1f);   // blue
        if (jointName.StartsWith("right_")) return new Color(1f,   0.4f, 0.3f); // red
        return Color.yellow;                                                      // center/head
    }

    public static Color GetBoneColor(string from, string to)
    {
        if (IsArmJoint(from) && IsArmJoint(to))
            return new Color(1f, 0.55f, 0.1f);   // orange — arms

        if (IsLegJoint(from) && IsLegJoint(to))
        {
            bool left = from.StartsWith("left_") || to.StartsWith("left_");
            return left
                ? new Color(0.2f,  0.6f,  1f)    // blue  — left leg
                : new Color(1f,    0.35f, 0.25f); // red   — right leg
        }

        return new Color(0.75f, 0.75f, 0.75f);   // gray  — torso
    }

    private static bool IsArmJoint(string n) =>
        n.Contains("shoulder") || n.Contains("elbow") || n.Contains("wrist");

    private static bool IsLegJoint(string n) =>
        n.Contains("hip") || n.Contains("knee") || n.Contains("ankle")
        || n.Contains("heel") || n.Contains("foot");
}

using System.Collections.Generic;
using UnityEngine;

public class MotionFrame
{
    public Dictionary<string, Vector3> joints = new Dictionary<string, Vector3>();
}

public class MotionClip
{
    public int fps = 30;
    public MotionFrame[] frames = new MotionFrame[0];

    public float Duration => frames.Length > 1 ? (float)(frames.Length - 1) / fps : 0f;
}

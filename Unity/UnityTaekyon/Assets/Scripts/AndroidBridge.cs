using UnityEngine;

// Receives JSON motion strings from Android via UnitySendMessage and drives playback.
// The GameObject holding this component must be named "AndroidBridge" in the scene.
public class AndroidBridge : MonoBehaviour
{
    private MotionPlayer _player;

    private void Start()
    {
        _player = FindFirstObjectByType<MotionPlayer>();
        if (_player == null)
            Debug.LogError("AndroidBridge: no MotionPlayer found in scene.");
    }

    // Called by Android: UnityPlayer.UnitySendMessage("AndroidBridge", "ReceiveMotionMessage", json)
    public void ReceiveMotionMessage(string json)
    {
        if (_player == null) return;

        MotionClip clip = MotionLoader.Load(json);
        if (clip == null || clip.frames.Length == 0)
        {
            Debug.LogWarning("AndroidBridge: received empty or invalid motion JSON.");
            return;
        }

        _player.Load(clip);
        _player.Play(loop: true);
    }
}

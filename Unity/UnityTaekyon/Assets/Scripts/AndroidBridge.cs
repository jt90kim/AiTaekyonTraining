using UnityEngine;

// Receives JSON motion strings from Android via UnitySendMessage and drives playback.
// The GameObject holding this component must be named "AndroidBridge" in the scene.
public class AndroidBridge : MonoBehaviour
{
    private MotionPlayer       _player;
    private MotionStateMachine _stateMachine;

    private void Start()
    {
        _player       = FindFirstObjectByType<MotionPlayer>();
        _stateMachine = FindFirstObjectByType<MotionStateMachine>();
        if (_player == null)
            Debug.LogError("AndroidBridge: no MotionPlayer found in scene.");

#if UNITY_ANDROID && !UNITY_EDITOR
        try
        {
            using var jc = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            using var activity = jc.GetStatic<AndroidJavaObject>("currentActivity");
            activity.Call("onUnitySceneReady");
            Debug.Log("AndroidBridge: notified Android that scene is ready.");
        }
        catch (System.Exception e)
        {
            Debug.LogWarning("AndroidBridge: could not notify Android ready: " + e.Message);
        }
#endif
    }

    // Called by Android: UnitySendMessage("AndroidBridge", "SetEnabledMoves", "roundhouse_kick,split_kick")
    public void SetEnabledMoves(string csv)
    {
        Debug.Log($"AndroidBridge: SetEnabledMoves '{csv}'");
        _stateMachine?.SetEnabledMoves(csv);
    }

    // Called by Android: UnityPlayer.UnitySendMessage("AndroidBridge", "ReceiveMotionMessage", json)
    public void ReceiveMotionMessage(string json)
    {
        Debug.Log($"AndroidBridge: ReceiveMotionMessage called, JSON length={json?.Length ?? 0}");
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

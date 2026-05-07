using System;
using UnityEngine;

public class MotionPlayer : MonoBehaviour
{
    public event Action<MotionFrame> OnFrameReady;

    [SerializeField] private float blendDuration = 0.25f;

    private MotionClip _clip;
    private float _time;
    private bool _isPlaying;
    private bool _loop;

    private MotionFrame _blendFrom;
    private float _blendTime;
    private bool _isBlending;
    private float _activeBlendDuration;
    private float _playbackSpeed = 1f;

    public bool IsPlaying => _isPlaying;

    private void Start()
    {
        var mapper = GetComponent<SkeletonMapper>();
        if (mapper == null) mapper = FindFirstObjectByType<SkeletonMapper>();
        if (mapper != null)
        {
            OnFrameReady += mapper.ApplyFrame;
            Debug.Log($"MotionPlayer.Start: subscribed to mapper on '{mapper.gameObject.name}'");
        }
        else
            Debug.LogError("MotionPlayer.Start: no SkeletonMapper found — skeleton will not move.");
    }

    private void Update()
    {
        Tick(Time.deltaTime);
    }

    public void Load(MotionClip clip, float blendOverride = -1f, float speed = 1f)
    {
        if (_isPlaying && _clip != null && _clip.frames.Length > 0)
        {
            _blendFrom = GetInterpolatedFrame(_time);
            _blendTime = 0f;
            _isBlending = true;
        }
        else
        {
            _isBlending = false;
        }

        _activeBlendDuration = blendOverride >= 0f ? blendOverride : blendDuration;
        _playbackSpeed = speed;
        _clip = clip;
        _time = 0f;
        _isPlaying = false;
    }

    public void Play(bool loop = true)
    {
        if (_clip == null || _clip.frames.Length == 0) return;
        _loop = loop;
        _isPlaying = true;
    }

    public void Pause() => _isPlaying = false;

    public void Stop()
    {
        _isPlaying = false;
        _isBlending = false;
        _time = 0f;
    }

    public void Tick(float deltaTime)
    {
        if (!_isPlaying || _clip == null || _clip.frames.Length == 0) return;

        if (_isBlending)
        {
            _blendTime += deltaTime;
            float t = Mathf.SmoothStep(0f, 1f, Mathf.Clamp01(_blendTime / _activeBlendDuration));
            MotionFrame frame = Lerp(_blendFrom, GetInterpolatedFrame(0f), t);
            if (t >= 1f) _isBlending = false;
            OnFrameReady?.Invoke(frame);
            return;
        }

        _time += deltaTime * _playbackSpeed;
        float duration = _clip.Duration;

        if (duration > 0f && _time >= duration)
        {
            if (_loop)
                _time %= duration;
            else
            {
                _time = duration;
                _isPlaying = false;
            }
        }

        OnFrameReady?.Invoke(GetInterpolatedFrame(_time));
    }

    private MotionFrame GetInterpolatedFrame(float time)
    {
        float frameFloat = time * _clip.fps;
        int frameA = Mathf.Clamp(Mathf.FloorToInt(frameFloat), 0, _clip.frames.Length - 1);
        int frameB = Mathf.Clamp(frameA + 1,                   0, _clip.frames.Length - 1);
        float t = frameFloat - frameA;

        return Lerp(_clip.frames[frameA], _clip.frames[frameB], t);
    }

    private static MotionFrame Lerp(MotionFrame a, MotionFrame b, float t)
    {
        var result = new MotionFrame();
        foreach (var kv in a.joints)
        {
            result.joints[kv.Key] = b.joints.TryGetValue(kv.Key, out Vector3 posB)
                ? Vector3.Lerp(kv.Value, posB, t)
                : kv.Value;
        }
        return result;
    }
}

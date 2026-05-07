using System;
using System.Collections.Generic;
using UnityEngine;

[Serializable]
public class StanceDefinition
{
    public string    name;
    public TextAsset idleClip;
}

[Serializable]
public class TransitionDefinition
{
    public string    fromStance;
    public string    toStance;
    public TextAsset clip;
    public float     blendIn = 0.15f;
}

[Serializable]
public class MoveVariant
{
    public string    moveType;   // matches MoveType.id on Android, e.g. "roundhouse_kick"
    public string    fromStance; // "left_forward" or "right_forward" — never neutral
    public TextAsset clip;
    public float     blendIn  = 0.10f;
    public float     blendOut = 0.40f;
}

public class MotionStateMachine : MonoBehaviour
{
    [SerializeField] private StanceDefinition[]     stances;
    [SerializeField] private TransitionDefinition[] transitions;
    [SerializeField] private MoveVariant[]          moveVariants;

    [SerializeField] [Range(0f, 1f)] private float moveProbability       = 0.30f;
    [SerializeField] private float minIdleDuration       = 0.5f;
    [SerializeField] private float maxIdleDuration       = 1.2f;
    [SerializeField] private float blendToIdle           = 0.30f;
    [SerializeField] private float movePlaybackSpeed       = 2.5f;
    [SerializeField] private float transitionPlaybackSpeed = 2.0f;

    private enum State { Idling, Transitioning, PerformingMove }

    // Fixed alternating cycle: left_forward → neutral → right_forward → neutral → …
    private static readonly string[] CycleStances = { "left_forward", "neutral", "right_forward", "neutral" };

    private MotionPlayer    _player;
    private State           _state;
    private string          _currentStance;
    private string          _pendingStance;
    private MoveVariant     _activeMove;
    private int             _cycleIndex;
    private float           _timer;
    private HashSet<string> _enabledMoveTypes = new HashSet<string>();

    private void Start()
    {
        _player = GetComponent<MotionPlayer>();
        if (_player == null)
        {
            Debug.LogError("MotionStateMachine: MotionPlayer not found on this GameObject.");
            return;
        }
        // Enable all configured move types by default so kicks fire in the Editor.
        // Android overrides this at runtime via SetEnabledMoves().
        foreach (var mv in moveVariants)
            if (mv.moveType != null) _enabledMoveTypes.Add(mv.moveType);

        _currentStance = "neutral";
        _cycleIndex = 0;
        PlayIdleForStance("neutral", blend: 0f);
        _state = State.Idling;
        ScheduleIdle();
    }

    private void Update()
    {
        if (_player == null) return;

        switch (_state)
        {
            case State.Idling:
                _timer -= Time.deltaTime;
                if (_timer <= 0f)
                    Fire();
                break;

            case State.Transitioning:
            case State.PerformingMove:
                if (!_player.IsPlaying)
                    OnClipEnd();
                break;
        }
    }

    // Called by AndroidBridge: UnitySendMessage("AndroidBridge", "SetEnabledMoves", "roundhouse_kick,split_kick")
    public void SetEnabledMoves(string csv)
    {
        _enabledMoveTypes = new HashSet<string>(
            csv.Split(new[] { ',' }, StringSplitOptions.RemoveEmptyEntries));
        Debug.Log($"MotionStateMachine: enabled moves = [{string.Join(", ", _enabledMoveTypes)}]");
    }

    private void ScheduleIdle()
    {
        _timer = _currentStance == "neutral"
            ? 0f
            : UnityEngine.Random.Range(minIdleDuration, maxIdleDuration);
    }

    private void Fire()
    {
        // Moves only fire from a forward stance — never from neutral
        if (_currentStance != "neutral")
        {
            var candidates = new List<MoveVariant>();
            foreach (var mv in moveVariants)
            {
                if (_enabledMoveTypes.Contains(mv.moveType) && mv.fromStance == _currentStance)
                    candidates.Add(mv);
            }

            if (candidates.Count > 0 && UnityEngine.Random.value < moveProbability)
            {
                PerformMove(candidates[UnityEngine.Random.Range(0, candidates.Count)]);
                return;
            }
        }

        PerformTransition(NextStanceInCycle());
    }

    private string NextStanceInCycle()
    {
        string next = CycleStances[_cycleIndex % CycleStances.Length];
        _cycleIndex++;
        return next;
    }

    private void PerformTransition(string toStance)
    {
        TransitionDefinition td = FindTransition(_currentStance, toStance);
        _pendingStance = toStance;
        _state = State.Transitioning;
        Debug.Log($"MotionStateMachine: Transitioning {_currentStance} → {toStance}");

        if (td?.clip != null)
        {
            PlayClip(td.clip, loop: false, blend: td.blendIn, speed: transitionPlaybackSpeed);
        }
        else
        {
            // No transition clip assigned — skip directly to target idle
            _currentStance = toStance;
            PlayIdleForStance(_currentStance, blend: blendToIdle);
            _state = State.Idling;
            ScheduleIdle();
        }
    }

    private void PerformMove(MoveVariant mv)
    {
        _activeMove = mv;
        _state = State.PerformingMove;
        Debug.Log($"MotionStateMachine: PerformingMove '{mv.moveType}' from '{mv.fromStance}'");
        PlayClip(mv.clip, loop: false, blend: mv.blendIn, speed: movePlaybackSpeed);
    }

    private void OnClipEnd()
    {
        switch (_state)
        {
            case State.Transitioning:
                _currentStance = _pendingStance;
                Debug.Log($"MotionStateMachine: arrived at stance '{_currentStance}'");
                PlayIdleForStance(_currentStance, blend: blendToIdle);
                _state = State.Idling;
                ScheduleIdle();
                break;

            case State.PerformingMove:
                Debug.Log($"MotionStateMachine: move done, transitioning to neutral");
                _activeMove = null;
                PerformTransition(NextStanceInCycle());
                break;
        }
    }

    private void PlayIdleForStance(string stance, float blend)
    {
        StanceDefinition sd = FindStance(stance);
        if (sd?.idleClip != null)
            PlayClip(sd.idleClip, loop: true, blend: blend);
        else
            Debug.LogWarning($"MotionStateMachine: no idle clip for stance '{stance}'");
    }

    private void PlayClip(TextAsset asset, bool loop, float blend, float speed = 1f)
    {
        MotionClip clip = MotionLoader.Load(asset.text);
        if (clip == null || clip.frames.Length == 0)
        {
            Debug.LogWarning($"MotionStateMachine: failed to load clip '{asset.name}'");
            return;
        }
        _player.Load(clip, blendOverride: blend, speed: speed);
        _player.Play(loop: loop);
    }

    private StanceDefinition FindStance(string name)
    {
        foreach (var s in stances)
            if (s.name == name) return s;
        return null;
    }

    private TransitionDefinition FindTransition(string from, string to)
    {
        foreach (var t in transitions)
            if (t.fromStance == from && t.toStance == to) return t;
        return null;
    }
}

using UnityEngine;

public class MotionStateMachine : MonoBehaviour
{
    [SerializeField] private TextAsset neutralClip;
    [SerializeField] private TextAsset leftStepClip;
    [SerializeField] private TextAsset rightStepClip;
    [SerializeField] private TextAsset leftKickClip;
    [SerializeField] private TextAsset rightKickClip;

    [SerializeField] private float minStepInterval    = 1.5f;
    [SerializeField] private float maxStepInterval    = 3.0f;
    [SerializeField] private float anticipationDuration = 0.3f;
    [SerializeField][Range(0f, 1f)] private float kickProbability = 0.3f;

    [Header("Blend Durations")]
    [SerializeField] private float blendToStep    = 0.15f;
    [SerializeField] private float blendToNeutral = 0.30f;
    [SerializeField] private float blendToKick    = 0.10f;
    [SerializeField] private float blendFromKick  = 0.40f;

    private enum State { Neutral, Anticipating, SteppingLeft, SteppingRight, KickingLeft, KickingRight }

    private MotionPlayer _player;
    private State _state;
    private float _stepTimer;
    private float _anticipateTimer;
    private bool _anticipatingLeft;

    private void Start()
    {
        _player = GetComponent<MotionPlayer>();
        if (_player == null)
        {
            Debug.LogError("MotionStateMachine: MotionPlayer not found on this GameObject.");
            return;
        }
        ReturnToNeutral();
    }

    private void Update()
    {
        if (_player == null) return;

        if (_state == State.Neutral)
        {
            _stepTimer -= Time.deltaTime;
            if (_stepTimer <= 0f)
                BeginAnticipation();
        }
        else if (_state == State.Anticipating)
        {
            _anticipateTimer -= Time.deltaTime;
            if (_anticipateTimer <= 0f)
                FireStep();
        }
        else if (!_player.IsPlaying)
        {
            OnClipEnd();
        }
    }

    private void BeginAnticipation()
    {
        _anticipatingLeft  = Random.value < 0.5f;
        _anticipateTimer   = anticipationDuration;
        _state = State.Anticipating;
        Debug.Log($"MotionStateMachine: Neutral → Anticipating ({(_anticipatingLeft ? "left" : "right")})");
    }

    private void FireStep()
    {
        TextAsset clip = _anticipatingLeft ? leftStepClip : rightStepClip;
        _state = _anticipatingLeft ? State.SteppingLeft : State.SteppingRight;
        Debug.Log($"MotionStateMachine: Anticipating → {_state}");

        if (clip != null)
            PlayClip(clip, loop: false, blend: blendToStep);
        else
            ReturnToNeutral();
    }

    private void OnClipEnd()
    {
        switch (_state)
        {
            case State.SteppingLeft:
                if (Random.value < kickProbability)
                {
                    if (leftKickClip != null)
                    {
                        _state = State.KickingLeft;
                        Debug.Log("MotionStateMachine: SteppingLeft → KickingLeft");
                        PlayClip(leftKickClip, loop: false, blend: blendToKick);
                    }
                    else
                    {
                        Debug.Log("MotionStateMachine: SteppingLeft → kick rolled but leftKickClip not assigned, skipping");
                        ReturnToNeutral();
                    }
                }
                else
                    ReturnToNeutral();
                break;

            case State.SteppingRight:
                if (Random.value < kickProbability)
                {
                    if (rightKickClip != null)
                    {
                        _state = State.KickingRight;
                        Debug.Log("MotionStateMachine: SteppingRight → KickingRight");
                        PlayClip(rightKickClip, loop: false, blend: blendToKick);
                    }
                    else
                    {
                        Debug.Log("MotionStateMachine: SteppingRight → kick rolled but rightKickClip not assigned, skipping");
                        ReturnToNeutral();
                    }
                }
                else
                    ReturnToNeutral();
                break;

            case State.KickingLeft:
            case State.KickingRight:
                ReturnToNeutral(blend: blendFromKick);
                break;
        }
    }

    private void ReturnToNeutral(float blend = -1f)
    {
        _state = State.Neutral;
        _stepTimer = Random.Range(minStepInterval, maxStepInterval);
        Debug.Log($"MotionStateMachine: → Neutral (next step in {_stepTimer:F1}s)");

        if (neutralClip != null)
            PlayClip(neutralClip, loop: true, blend: blend >= 0f ? blend : blendToNeutral);
    }

    private void PlayClip(TextAsset asset, bool loop, float blend = -1f)
    {
        MotionClip clip = MotionLoader.Load(asset.text);
        if (clip == null || clip.frames.Length == 0)
        {
            Debug.LogWarning($"MotionStateMachine: failed to load clip '{asset.name}'");
            ReturnToNeutral();
            return;
        }
        _player.Load(clip, blendOverride: blend);
        _player.Play(loop: loop);
    }
}

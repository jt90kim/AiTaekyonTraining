using UnityEngine;

public class MotionStateMachine : MonoBehaviour
{
    [SerializeField] private TextAsset neutralClip;
    [SerializeField] private TextAsset leftStepClip;
    [SerializeField] private TextAsset rightStepClip;
    [SerializeField] private TextAsset leftKickClip;
    [SerializeField] private TextAsset rightKickClip;
    [SerializeField] private float minStepInterval = 1.5f;
    [SerializeField] private float maxStepInterval = 3.0f;
    [SerializeField][Range(0f, 1f)] private float kickProbability = 0.3f;

    private enum State { Neutral, SteppingLeft, SteppingRight, KickingLeft, KickingRight }

    private MotionPlayer _player;
    private State _state;
    private float _stepTimer;

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
                TakeStep();
        }
        else if (!_player.IsPlaying)
        {
            OnClipEnd();
        }
    }

    private void TakeStep()
    {
        bool goLeft = Random.value < 0.5f;
        TextAsset clip = goLeft ? leftStepClip : rightStepClip;
        _state = goLeft ? State.SteppingLeft : State.SteppingRight;
        Debug.Log($"MotionStateMachine: Neutral → {_state}");

        if (clip != null)
            PlayClip(clip, loop: false);
        else
            ReturnToNeutral();
    }

    private void OnClipEnd()
    {
        switch (_state)
        {
            case State.SteppingLeft:
                if (leftKickClip != null && Random.value < kickProbability)
                {
                    _state = State.KickingLeft;
                    Debug.Log("MotionStateMachine: SteppingLeft → KickingLeft");
                    PlayClip(leftKickClip, loop: false);
                }
                else
                    ReturnToNeutral();
                break;

            case State.SteppingRight:
                if (rightKickClip != null && Random.value < kickProbability)
                {
                    _state = State.KickingRight;
                    Debug.Log("MotionStateMachine: SteppingRight → KickingRight");
                    PlayClip(rightKickClip, loop: false);
                }
                else
                    ReturnToNeutral();
                break;

            case State.KickingLeft:
            case State.KickingRight:
                ReturnToNeutral();
                break;
        }
    }

    private void ReturnToNeutral()
    {
        _state = State.Neutral;
        _stepTimer = Random.Range(minStepInterval, maxStepInterval);
        Debug.Log($"MotionStateMachine: → Neutral (next step in {_stepTimer:F1}s)");

        if (neutralClip != null)
            PlayClip(neutralClip, loop: true);
    }

    private void PlayClip(TextAsset asset, bool loop)
    {
        MotionClip clip = MotionLoader.Load(asset.text);
        if (clip == null || clip.frames.Length == 0)
        {
            Debug.LogWarning($"MotionStateMachine: failed to load clip '{asset.name}'");
            ReturnToNeutral();
            return;
        }
        _player.Load(clip);
        _player.Play(loop: loop);
    }
}

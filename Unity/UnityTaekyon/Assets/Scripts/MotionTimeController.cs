using UnityEngine;
using UnityEngine.InputSystem;

public class MotionTimeController : MonoBehaviour
{
    [SerializeField] private TextAsset sampleClip;

    private MotionPlayer _player;
    private SkeletonMapper _mapper;

    private void Awake()
    {
        _player = GetComponent<MotionPlayer>();
        _mapper  = GetComponent<SkeletonMapper>();
        _mapper  = GetComponent<SkeletonMapper>();
        Debug.Log($"MotionTimeController.Awake: player={(_player == null ? "NULL" : "OK")}, mapper={(_mapper == null ? "NULL" : "OK")}");
    }

    private void Start()
    {
        if (_player == null || _mapper == null)
        {
            Debug.LogError("MotionTimeController: missing MotionPlayer or SkeletonMapper on this GameObject — aborting Start.");
            return;
        }

        _player.OnFrameReady += _mapper.ApplyFrame;

        if (sampleClip == null)
        {
            Debug.LogWarning("MotionTimeController: sampleClip not assigned in Inspector — skeleton will not show until a motion is sent from Android.");
            return;
        }

        MotionClip clip = MotionLoader.Load(sampleClip.text);
        Debug.Log($"MotionTimeController.Start: sampleClip loaded, frames={clip.frames.Length}");
        if (clip.frames.Length == 0) return;

        _mapper.ApplyFrame(clip.frames[0]);
        _player.Load(clip);
        _player.Play(loop: true);
    }

    private void Update()
    {
        _player.Tick(Time.deltaTime);
        // TODO: Temporary, remove this later
        if (Keyboard.current.spaceKey.wasPressedThisFrame)
        {
            MotionClip clip = MotionLoader.Load(sampleClip.text);
            _player.Load(clip);
            _player.Play(loop: true);
        }
    }
}

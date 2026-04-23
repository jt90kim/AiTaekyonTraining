using UnityEngine;

public class MotionTimeController : MonoBehaviour
{
    private MotionPlayer _player;
    private SkeletonMapper _mapper;

    private void Awake()
    {
        _player = GetComponent<MotionPlayer>();
        _mapper  = GetComponent<SkeletonMapper>();
    }

    private void Start()
    {
        if (_player == null || _mapper == null)
        {
            Debug.LogError("MotionTimeController: missing MotionPlayer or SkeletonMapper on this GameObject.");
            return;
        }
        _player.OnFrameReady += _mapper.ApplyFrame;
    }

    private void Update()
    {
        _player.Tick(Time.deltaTime);
    }
}

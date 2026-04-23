using UnityEngine;

public class MotionTimeController : MonoBehaviour
{
    [SerializeField] private TextAsset sampleClip;

    private MotionPlayer _player;
    private SkeletonMapper _mapper;

    private void Awake()
    {
        _player = GetComponent<MotionPlayer>();
        _mapper  = GetComponent<SkeletonMapper>();
    }

    private void Start()
    {
        _player.OnFrameReady += _mapper.ApplyFrame;

        if (sampleClip == null) return;

        MotionClip clip = MotionLoader.Load(sampleClip.text);
        if (clip.frames.Length == 0) return;

        _mapper.ApplyFrame(clip.frames[0]);
        _player.Load(clip);
        _player.Play(loop: true);
    }

    private void Update()
    {
        _player.Tick(Time.deltaTime);
    }
}

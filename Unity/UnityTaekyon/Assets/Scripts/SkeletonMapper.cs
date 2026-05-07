using System.Collections.Generic;
using UnityEngine;

public class SkeletonMapper : MonoBehaviour
{
    [SerializeField] private float yRotationOffset = 0f;

    private readonly Dictionary<string, Transform> _joints = new Dictionary<string, Transform>();

    private void Awake()
    {
        foreach (string name in SkeletonDefinition.JointNames)
        {
            var go = new GameObject(name);
            go.transform.SetParent(transform, worldPositionStays: false);
            _joints[name] = go.transform;
        }
    }

    public void ApplyFrame(MotionFrame frame)
    {
        Quaternion rot = Quaternion.Euler(0f, yRotationOffset, 0f);
        foreach (var kv in frame.joints)
        {
            if (_joints.TryGetValue(kv.Key, out Transform t))
                t.localPosition = rot * kv.Value;
        }
    }

    public Transform GetJoint(string name)
    {
        _joints.TryGetValue(name, out Transform t);
        return t;
    }

    public IEnumerable<KeyValuePair<string, Transform>> AllJoints => _joints;
}

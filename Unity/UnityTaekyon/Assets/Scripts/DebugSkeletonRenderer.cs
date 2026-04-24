using System.Collections.Generic;
using UnityEngine;

public class DebugSkeletonRenderer : MonoBehaviour
{
    [SerializeField] private SkeletonMapper mapper;
    [SerializeField] private float jointRadius = 0.04f;
    [SerializeField] private float lineWidth    = 0.015f;

    // Assign a URP Lit material and a URP Unlit material in the Inspector.
    // Using serialized material templates guarantees the shaders are included in the Android build.
    [SerializeField] private Material jointMaterialTemplate;
    [SerializeField] private Material boneMaterialTemplate;

    [SerializeField] private float anticipationPulseSpeed = 6f;
    [SerializeField] private Color anticipationColor = new Color(1f, 0.6f, 0f); // orange

    private readonly Dictionary<string, GameObject> _spheres = new Dictionary<string, GameObject>();
    private readonly Dictionary<string, (MeshRenderer mr, Color baseColor)> _sphereData
        = new Dictionary<string, (MeshRenderer, Color)>();
    private readonly List<(LineRenderer lr, string from, string to, Color baseColor)> _bones
        = new List<(LineRenderer, string, string, Color)>();

    private bool _anticipating;
    private bool _wasAnticipating;
    private int _lateUpdateCount;

    private void Awake()
    {
        if (mapper == null) mapper = GetComponent<SkeletonMapper>();
    }

    private void Start()
    {
        if (jointMaterialTemplate == null || boneMaterialTemplate == null)
            Debug.LogError("DebugSkeletonRenderer: jointMaterialTemplate or boneMaterialTemplate not assigned in Inspector — skeleton will be invisible on Android.");

        CreateJointSpheres();
        CreateBoneLines();
        Debug.Log($"DebugSkeletonRenderer.Start: created {_spheres.Count} spheres, {_bones.Count} bones. jointMat={jointMaterialTemplate != null}, boneMat={boneMaterialTemplate != null}");
    }

    public void SetAnticipating(bool on)
    {
        _anticipating = on;
    }

    private void LateUpdate()
    {
        UpdateJointSpheres();
        UpdateBoneLines();
        UpdateAnticipationPulse();

        if (_lateUpdateCount < 3)
        {
            Transform nose = mapper.GetJoint("nose");
            Debug.Log($"DebugSkeletonRenderer.LateUpdate[{_lateUpdateCount}]: nose world pos={nose?.position.ToString() ?? "null"}");
            _lateUpdateCount++;
        }
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    private void CreateJointSpheres()
    {
        foreach (string name in SkeletonDefinition.JointNames)
        {
            Transform joint = mapper.GetJoint(name);
            if (joint == null) continue;

            var sphere = GameObject.CreatePrimitive(PrimitiveType.Sphere);
            sphere.name = "sphere_" + name;
            sphere.transform.SetParent(transform);
            sphere.transform.localScale = Vector3.one * (jointRadius * 2f);
            Destroy(sphere.GetComponent<Collider>());

            var mr = sphere.GetComponent<MeshRenderer>();
            Color baseColor = Color.white;
            if (jointMaterialTemplate != null)
            {
                var mat = new Material(jointMaterialTemplate);
                baseColor = SkeletonDefinition.GetJointColor(name);
                mat.color = baseColor;
                mr.material = mat;
            }

            _spheres[name] = sphere;
            _sphereData[name] = (mr, baseColor);
        }
    }

    private void CreateBoneLines()
    {
        foreach (var (from, to) in SkeletonDefinition.Bones)
        {
            if (mapper.GetJoint(from) == null || mapper.GetJoint(to) == null) continue;

            var go = new GameObject($"bone_{from}_{to}");
            go.transform.SetParent(transform);

            var lr = go.AddComponent<LineRenderer>();
            lr.positionCount = 2;
            lr.startWidth = lineWidth;
            lr.endWidth   = lineWidth;
            lr.useWorldSpace = true;

            Color baseColor = Color.white;
            if (boneMaterialTemplate != null)
            {
                var mat = new Material(boneMaterialTemplate);
                baseColor = SkeletonDefinition.GetBoneColor(from, to);
                mat.color = baseColor;
                lr.material = mat;
            }

            _bones.Add((lr, from, to, baseColor));
        }
    }

    // ── Per-frame update ─────────────────────────────────────────────────────

    private void UpdateJointSpheres()
    {
        foreach (var kv in _spheres)
        {
            Transform joint = mapper.GetJoint(kv.Key);
            if (joint != null)
                kv.Value.transform.position = joint.position;
        }
    }

    private void UpdateBoneLines()
    {
        foreach (var (lr, from, to, _) in _bones)
        {
            Transform a = mapper.GetJoint(from);
            Transform b = mapper.GetJoint(to);
            if (a == null || b == null) continue;
            lr.SetPosition(0, a.position);
            lr.SetPosition(1, b.position);
        }
    }

    private void UpdateAnticipationPulse()
    {
        if (_anticipating)
        {
            float pulse = (Mathf.Sin(Time.time * Mathf.PI * anticipationPulseSpeed) + 1f) * 0.5f;
            Color tint = Color.Lerp(Color.white, anticipationColor, pulse);
            ApplyTint(tint);
            _wasAnticipating = true;
        }
        else if (_wasAnticipating)
        {
            ApplyTint(Color.white);
            _wasAnticipating = false;
        }
    }

    private void ApplyTint(Color tint)
    {
        foreach (var kv in _sphereData)
            kv.Value.mr.material.color = kv.Value.baseColor * tint;

        foreach (var (lr, _, _, baseColor) in _bones)
            lr.material.color = baseColor * tint;
    }
}

using System.Collections.Generic;
using UnityEngine;

public class DebugSkeletonRenderer : MonoBehaviour
{
    [SerializeField] private SkeletonMapper mapper;
    [SerializeField] private float jointRadius = 0.04f;
    [SerializeField] private float lineWidth    = 0.015f;

    private readonly Dictionary<string, GameObject> _spheres = new Dictionary<string, GameObject>();
    private readonly List<(LineRenderer lr, string from, string to)> _bones
        = new List<(LineRenderer, string, string)>();

    private void Awake()
    {
        if (mapper == null) mapper = GetComponent<SkeletonMapper>();
    }

    private void Start()
    {
        CreateJointSpheres();
        CreateBoneLines();
    }

    private void LateUpdate()
    {
        UpdateJointSpheres();
        UpdateBoneLines();
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    private static Shader SafeShader(string urpName, string builtinFallback)
    {
        var s = Shader.Find(urpName);
        return s != null ? s : Shader.Find(builtinFallback);
    }

    private void CreateJointSpheres()
    {
        Shader litShader = SafeShader("Universal Render Pipeline/Lit", "Standard");
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
            var mat = new Material(litShader);
            Color c = SkeletonDefinition.GetJointColor(name);
            mat.SetColor("_BaseColor", c);
            mat.color = c;
            mr.material = mat;

            _spheres[name] = sphere;
        }
    }

    private void CreateBoneLines()
    {
        Material lineMat = new Material(SafeShader("Universal Render Pipeline/Unlit", "Unlit/Color"));

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

            var mat = new Material(lineMat);
            Color c = SkeletonDefinition.GetBoneColor(from, to);
            mat.SetColor("_BaseColor", c);
            lr.material = mat;

            _bones.Add((lr, from, to));
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
        foreach (var (lr, from, to) in _bones)
        {
            Transform a = mapper.GetJoint(from);
            Transform b = mapper.GetJoint(to);
            if (a == null || b == null) continue;
            lr.SetPosition(0, a.position);
            lr.SetPosition(1, b.position);
        }
    }
}

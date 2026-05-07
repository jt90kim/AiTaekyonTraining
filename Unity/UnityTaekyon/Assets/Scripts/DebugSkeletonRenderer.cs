using System.Collections.Generic;
using UnityEngine;

public class DebugSkeletonRenderer : MonoBehaviour
{
    [SerializeField] private SkeletonMapper mapper;
    [SerializeField] private float boneRadius = 0.04f;
    [SerializeField] private float headRadius = 0.08f;

    [SerializeField] private Material jointMaterialTemplate;
    [SerializeField] private Material boneMaterialTemplate;

    private GameObject _headSphere;
    private readonly List<(Transform bone, string from, string to)> _bones
        = new List<(Transform, string, string)>();

    private void Awake()
    {
        if (mapper == null) mapper = GetComponent<SkeletonMapper>();
    }

    private void Start()
    {
        if (jointMaterialTemplate == null || boneMaterialTemplate == null)
            Debug.LogError("DebugSkeletonRenderer: material templates not assigned — skeleton will be invisible on Android.");

        CreateHeadSphere();
        CreateBoneCapsules();
    }

    private void LateUpdate()
    {
        UpdateHeadSphere();
        UpdateBoneCapsules();
    }

    private void CreateHeadSphere()
    {
        _headSphere = GameObject.CreatePrimitive(PrimitiveType.Sphere);
        _headSphere.name = "head";
        _headSphere.transform.SetParent(transform);
        _headSphere.transform.localScale = Vector3.one * (headRadius * 2f);
        Destroy(_headSphere.GetComponent<Collider>());

        if (jointMaterialTemplate != null)
        {
            var mat = new Material(jointMaterialTemplate);
            mat.color = Color.yellow;
            _headSphere.GetComponent<MeshRenderer>().material = mat;
        }
    }

    private void CreateBoneCapsules()
    {
        foreach (var (from, to) in SkeletonDefinition.Bones)
        {
            if (mapper.GetJoint(from) == null || mapper.GetJoint(to) == null) continue;

            var go = GameObject.CreatePrimitive(PrimitiveType.Cylinder);
            go.name = $"bone_{from}_{to}";
            go.transform.SetParent(transform);
            Destroy(go.GetComponent<Collider>());

            if (boneMaterialTemplate != null)
            {
                var mat = new Material(boneMaterialTemplate);
                mat.color = SkeletonDefinition.GetBoneColor(from, to);
                go.GetComponent<MeshRenderer>().material = mat;
            }

            _bones.Add((go.transform, from, to));
        }
    }

    private void UpdateHeadSphere()
    {
        Transform nose = mapper.GetJoint("nose");
        if (nose != null)
            _headSphere.transform.position = nose.position;
    }

    private void UpdateBoneCapsules()
    {
        foreach (var (bone, from, to) in _bones)
        {
            Transform a = mapper.GetJoint(from);
            Transform b = mapper.GetJoint(to);
            if (a == null || b == null) continue;

            Vector3 dir = b.position - a.position;
            float length = dir.magnitude;
            if (length < 0.001f) continue;

            bone.position = (a.position + b.position) * 0.5f;
            bone.rotation = Quaternion.FromToRotation(Vector3.up, dir.normalized);
            // Unity cylinder height = 2 at localScale.y = 1, so use length * 0.5
            bone.localScale = new Vector3(boneRadius * 2f, length * 0.5f, boneRadius * 2f);
        }
    }
}

using System.Collections.Generic;
using UnityEngine;

[RequireComponent(typeof(SkeletonMapper))]
public class MannequinRenderer : MonoBehaviour
{
    [SerializeField] private Color bodyColor = new Color(0.72f, 0.70f, 0.68f, 1f);

    private SkeletonMapper _mapper;
    private Mesh _mesh;
    private readonly List<Vector3> _verts   = new List<Vector3>();
    private readonly List<Vector3> _normals = new List<Vector3>();
    private readonly List<int>     _tris    = new List<int>();

    private void Awake()
    {
        _mapper = GetComponent<SkeletonMapper>();
    }

    private void Start()
    {
        _mesh = new Mesh { name = "MannequinMesh" };

        var go = new GameObject("MannequinMesh");
        go.transform.SetParent(transform, worldPositionStays: false);
        go.transform.localPosition = Vector3.zero;
        go.transform.localRotation = Quaternion.identity;
        go.transform.localScale    = Vector3.one;

        go.AddComponent<MeshFilter>().sharedMesh = _mesh;

        var mr  = go.AddComponent<MeshRenderer>();
        var mat = new Material(Shader.Find("Universal Render Pipeline/Lit"));
        mat.color = bodyColor;
        mat.SetFloat("_Smoothness", 0f);
        mat.SetFloat("_Metallic",   0f);
        mr.sharedMaterial = mat;

        CreateFloorGrid(mat);
    }

    private void LateUpdate() => RebuildMesh();

    // ── Mesh rebuild ──────────────────────────────────────────────────────────

    private void RebuildMesh()
    {
        _verts.Clear();
        _normals.Clear();
        _tris.Clear();

        Transform nose  = _mapper.GetJoint("nose");
        Transform ls    = _mapper.GetJoint("left_shoulder");
        Transform rs    = _mapper.GetJoint("right_shoulder");
        Transform le    = _mapper.GetJoint("left_elbow");
        Transform re    = _mapper.GetJoint("right_elbow");
        Transform lw    = _mapper.GetJoint("left_wrist");
        Transform rw    = _mapper.GetJoint("right_wrist");
        Transform lh    = _mapper.GetJoint("left_hip");
        Transform rh    = _mapper.GetJoint("right_hip");
        Transform lk    = _mapper.GetJoint("left_knee");
        Transform rk    = _mapper.GetJoint("right_knee");
        Transform la    = _mapper.GetJoint("left_ankle");
        Transform ra    = _mapper.GetJoint("right_ankle");
        Transform lheel = _mapper.GetJoint("left_heel");
        Transform rheel = _mapper.GetJoint("right_heel");
        Transform lfi   = _mapper.GetJoint("left_foot_index");
        Transform rfi   = _mapper.GetJoint("right_foot_index");

        if (ls == null || rs == null) return;

        // Head
        Vector3 shoulderMid = (ls.position + rs.position) * 0.5f;
        Vector3 headCenter  = nose != null
            ? new Vector3(shoulderMid.x, nose.position.y, shoulderMid.z)
            : shoulderMid + Vector3.up * 0.14f;
        AddSphere(headCenter, 0.10f);

        // Torso
        if (lh != null && rh != null)
            AddTorso(ls.position, rs.position, lh.position, rh.position);

        // Arms
        if (le != null) AddLimbSegment(ls.position, le.position, 0.045f, 0.035f, 6);
        if (le != null && lw != null) AddLimbSegment(le.position, lw.position, 0.035f, 0.026f, 6);
        if (lw != null) AddCap(lw.position, ls.position, 0.026f, 6);

        if (re != null) AddLimbSegment(rs.position, re.position, 0.045f, 0.035f, 6);
        if (re != null && rw != null) AddLimbSegment(re.position, rw.position, 0.035f, 0.026f, 6);
        if (rw != null) AddCap(rw.position, rs.position, 0.026f, 6);

        // Legs
        if (lh != null && lk != null) AddLimbSegment(lh.position, lk.position, 0.065f, 0.050f, 8);
        if (lk != null && la != null) AddLimbSegment(lk.position, la.position, 0.045f, 0.030f, 8);
        if (la != null && lheel != null && lfi != null)
            AddFoot(la.position, lheel.position, lfi.position);

        if (rh != null && rk != null) AddLimbSegment(rh.position, rk.position, 0.065f, 0.050f, 8);
        if (rk != null && ra != null) AddLimbSegment(rk.position, ra.position, 0.045f, 0.030f, 8);
        if (ra != null && rheel != null && rfi != null)
            AddFoot(ra.position, rheel.position, rfi.position);

        _mesh.Clear();
        _mesh.SetVertices(_verts);
        _mesh.SetNormals(_normals);
        _mesh.SetTriangles(_tris, 0);
    }

    // ── Geometry helpers ──────────────────────────────────────────────────────

    private void AddFlatTri(Vector3 a, Vector3 b, Vector3 c)
    {
        Vector3 n = Vector3.Cross(b - a, c - a).normalized;
        int i = _verts.Count;
        _verts.Add(a);   _normals.Add(n);
        _verts.Add(b);   _normals.Add(n);
        _verts.Add(c);   _normals.Add(n);
        _tris.Add(i); _tris.Add(i + 1); _tris.Add(i + 2);
    }

    private void AddLimbSegment(Vector3 from, Vector3 to, float r1, float r2, int sides)
    {
        Vector3 axis = (to - from).normalized;
        if (axis == Vector3.zero) return;

        Vector3 perp = Vector3.Cross(axis, Vector3.up);
        if (perp.sqrMagnitude < 0.001f) perp = Vector3.Cross(axis, Vector3.right);
        perp.Normalize();
        Vector3 perp2 = Vector3.Cross(axis, perp).normalized;

        var ring1 = new Vector3[sides];
        var ring2 = new Vector3[sides];
        for (int i = 0; i < sides; i++)
        {
            float a = i * Mathf.PI * 2f / sides;
            float c = Mathf.Cos(a), s = Mathf.Sin(a);
            ring1[i] = from + (perp * c + perp2 * s) * r1;
            ring2[i] = to   + (perp * c + perp2 * s) * r2;
        }

        for (int i = 0; i < sides; i++)
        {
            int j = (i + 1) % sides;
            AddFlatTri(ring1[i], ring2[i], ring1[j]);
            AddFlatTri(ring1[j], ring2[i], ring2[j]);
        }
    }

    private void AddTorso(Vector3 ls, Vector3 rs, Vector3 lh, Vector3 rh)
    {
        Vector3 shMid  = (ls + rs) * 0.5f;
        Vector3 hipMid = (lh + rh) * 0.5f;
        float   depth  = (rs - ls).magnitude * 0.20f;

        Vector3 fwd = Vector3.Cross((rs - ls).normalized, Vector3.up).normalized * depth;

        // 8 corners: top-front, top-back, bottom-front, bottom-back × left/right
        Vector3 ltf = ls + fwd, ltb = ls - fwd;
        Vector3 rtf = rs + fwd, rtb = rs - fwd;
        Vector3 lbf = lh + fwd, lbb = lh - fwd;
        Vector3 rbf = rh + fwd, rbb = rh - fwd;

        // Front
        AddFlatTri(ltf, rtf, lbf); AddFlatTri(rtf, rbf, lbf);
        // Back
        AddFlatTri(rtb, ltb, rbb); AddFlatTri(ltb, lbb, rbb);
        // Left
        AddFlatTri(ltb, ltf, lbb); AddFlatTri(ltf, lbf, lbb);
        // Right
        AddFlatTri(rtf, rtb, rbf); AddFlatTri(rtb, rbb, rbf);
        // Top
        AddFlatTri(ltb, rtb, ltf); AddFlatTri(rtb, rtf, ltf);
        // Bottom
        AddFlatTri(lbf, rbf, lbb); AddFlatTri(rbf, rbb, lbb);
    }

    private void AddSphere(Vector3 center, float radius, int lat = 5, int lon = 8)
    {
        // Build vertex grid [lat+1][lon]
        var pts = new Vector3[lat + 1, lon];
        for (int i = 0; i <= lat; i++)
        {
            float phi = Mathf.PI * i / lat - Mathf.PI * 0.5f; // -PI/2 .. +PI/2
            float y   = Mathf.Sin(phi) * radius;
            float r   = Mathf.Cos(phi) * radius;
            for (int j = 0; j < lon; j++)
            {
                float theta = j * Mathf.PI * 2f / lon;
                pts[i, j] = center + new Vector3(r * Mathf.Cos(theta), y, r * Mathf.Sin(theta));
            }
        }

        // South pole cap (row 0 → row 1)
        for (int j = 0; j < lon; j++)
        {
            int jn = (j + 1) % lon;
            AddFlatTri(pts[0, j], pts[1, j], pts[1, jn]);
        }

        // Middle quads
        for (int i = 1; i < lat - 1; i++)
            for (int j = 0; j < lon; j++)
            {
                int jn = (j + 1) % lon;
                AddFlatTri(pts[i, j],  pts[i + 1, j],  pts[i, jn]);
                AddFlatTri(pts[i, jn], pts[i + 1, j],  pts[i + 1, jn]);
            }

        // North pole cap (row lat-1 → row lat)
        for (int j = 0; j < lon; j++)
        {
            int jn = (j + 1) % lon;
            AddFlatTri(pts[lat - 1, j], pts[lat, j], pts[lat - 1, jn]);
        }
    }

    private void AddFoot(Vector3 ankle, Vector3 heel, Vector3 footIndex)
    {
        float w = 0.04f;
        Vector3 fwd  = (footIndex - heel).normalized;
        Vector3 side = Vector3.Cross(fwd, Vector3.up).normalized * w;
        Vector3 up   = Vector3.up * 0.025f;

        Vector3 hl = heel      - side, hr = heel      + side;
        Vector3 fl = footIndex - side, fr = footIndex + side;

        AddFlatTri(hl, hr + up, hl + up); AddFlatTri(hl, hr, hr + up);
        AddFlatTri(hl, fl, fr);           AddFlatTri(hl, fr, hr);
        AddFlatTri(hl + up, hr + up, fr + up); AddFlatTri(hl + up, fr + up, fl + up);
        AddFlatTri(fl, fl + up, fr + up); AddFlatTri(fl, fr + up, fr);
        AddFlatTri(hl, hl + up, fl + up); AddFlatTri(hl, fl + up, fl);
        AddFlatTri(hr + up, hr, fr);      AddFlatTri(hr + up, fr, fr + up);
    }

    private void AddCap(Vector3 tip, Vector3 toward, float radius, int sides)
    {
        Vector3 axis = (tip - toward).normalized;
        if (axis == Vector3.zero) return;

        Vector3 perp = Vector3.Cross(axis, Vector3.up);
        if (perp.sqrMagnitude < 0.001f) perp = Vector3.Cross(axis, Vector3.right);
        perp.Normalize();
        Vector3 perp2 = Vector3.Cross(axis, perp).normalized;
        Vector3 apex  = tip + axis * radius;

        for (int i = 0; i < sides; i++)
        {
            int j = (i + 1) % sides;
            float a1 = i * Mathf.PI * 2f / sides, a2 = j * Mathf.PI * 2f / sides;
            Vector3 v1 = tip + (perp * Mathf.Cos(a1) + perp2 * Mathf.Sin(a1)) * radius;
            Vector3 v2 = tip + (perp * Mathf.Cos(a2) + perp2 * Mathf.Sin(a2)) * radius;
            AddFlatTri(v1, apex, v2);
        }
    }

    // ── Floor grid (static, created once) ────────────────────────────────────

    private void CreateFloorGrid(Material matTemplate)
    {
        const int   tiles    = 8;
        const float tileSize = 0.5f;

        var tex = new Texture2D(tiles, tiles, TextureFormat.RGB24, false);
        tex.filterMode = FilterMode.Point;
        for (int x = 0; x < tiles; x++)
            for (int z = 0; z < tiles; z++)
                tex.SetPixel(x, z, (x + z) % 2 == 0 ? Color.white : Color.black);
        tex.Apply();

        var go = GameObject.CreatePrimitive(PrimitiveType.Plane);
        go.name = "FloorGrid";
        Destroy(go.GetComponent<Collider>());

        float scale = tiles * tileSize / 10f;
        go.transform.localScale = new Vector3(scale, 1f, scale);

        var mat = new Material(matTemplate.shader);
        mat.color = Color.white;
        mat.SetFloat("_Smoothness", 0f);
        mat.SetTexture("_BaseMap", tex);
        go.GetComponent<MeshRenderer>().sharedMaterial = mat;
    }
}

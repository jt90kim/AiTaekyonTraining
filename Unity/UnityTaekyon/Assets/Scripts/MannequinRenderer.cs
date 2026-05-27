using System.Collections.Generic;
using UnityEngine;

[RequireComponent(typeof(SkeletonMapper))]
public class MannequinRenderer : MonoBehaviour
{
    [SerializeField] private Color bodyColor = new Color(0.76f, 0.74f, 0.72f, 1f);

    private SkeletonMapper _mapper;
    private Mesh           _mesh;

    private readonly List<Vector3> _verts   = new List<Vector3>(2048);
    private readonly List<Vector3> _normals = new List<Vector3>(2048);
    private readonly List<int>     _tris    = new List<int>(4096);

    private void Awake() => _mapper = GetComponent<SkeletonMapper>();

    private void Start()
    {
        _mesh = new Mesh { name = "MannequinMesh" };

        var go = new GameObject("MannequinMesh");
        go.transform.SetParent(transform, worldPositionStays: false);

        go.AddComponent<MeshFilter>().sharedMesh = _mesh;

        var mr  = go.AddComponent<MeshRenderer>();
        var mat = new Material(Shader.Find("Universal Render Pipeline/Lit"));
        mat.color = bodyColor;
        mat.SetFloat("_Smoothness", 0f);
        mat.SetFloat("_Metallic",   0f);
        mr.sharedMaterial = mat;

        CreateFloorGrid(mat.shader);
    }

    private void LateUpdate() => RebuildMesh();

    // ── Rebuild ───────────────────────────────────────────────────────────────

    private void RebuildMesh()
    {
        _verts.Clear(); _normals.Clear(); _tris.Clear();

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

        if (ls == null || rs == null || lh == null || rh == null) return;

        Vector3 lsP = ls.position, rsP = rs.position;
        Vector3 lhP = lh.position, rhP = rh.position;
        Vector3 shoulderMid = (lsP + rsP) * 0.5f;
        Vector3 hipMid      = (lhP + rhP) * 0.5f;

        // Stable torso basis
        Vector3 torsoUp    = (shoulderMid - hipMid); torsoUp = torsoUp.sqrMagnitude > 0.0001f ? torsoUp.normalized : Vector3.up;
        Vector3 torsoRight = (rsP - lsP);            torsoRight = torsoRight.sqrMagnitude > 0.0001f ? torsoRight.normalized : Vector3.right;
        Vector3 torsoFwd   = Vector3.Cross(torsoRight, torsoUp).normalized;

        // ── Head & neck ──────────────────────────────────────────────────────
        Vector3 neckBase = shoulderMid + torsoUp * 0.05f;
        Vector3 headPos  = nose != null
            ? new Vector3(shoulderMid.x, nose.position.y, shoulderMid.z)
            : neckBase + torsoUp * 0.13f;
        Vector3 neckTop  = headPos  - torsoUp * 0.05f;

        AddSphere(headPos, 0.095f, 5, 10);
        AddFrustum(neckBase, neckTop, 0.034f, 0.036f, 8);

        // ── Torso ─────────────────────────────────────────────────────────────
        AddTorso(lsP, rsP, lhP, rhP, torsoRight, torsoFwd);

        // ── Arms ──────────────────────────────────────────────────────────────
        if (le != null)
        {
            AddFrustum(lsP, le.position, 0.050f, 0.038f, 8);
            if (lw != null) AddFrustum(le.position, lw.position, 0.036f, 0.025f, 8);
            if (lw != null) AddHand(lw.position, le.position, 0.024f);
        }
        if (re != null)
        {
            AddFrustum(rsP, re.position, 0.050f, 0.038f, 8);
            if (rw != null) AddFrustum(re.position, rw.position, 0.036f, 0.025f, 8);
            if (rw != null) AddHand(rw.position, re.position, 0.024f);
        }

        // ── Legs ──────────────────────────────────────────────────────────────
        if (lk != null)
        {
            AddFrustum(lhP, lk.position, 0.062f, 0.050f, 9);
            if (la != null)
            {
                AddFrustum(lk.position, la.position, 0.046f, 0.030f, 9);
                if (lheel != null && lfi != null) AddFoot(la.position, lheel.position, lfi.position);
            }
        }
        if (rk != null)
        {
            AddFrustum(rhP, rk.position, 0.062f, 0.050f, 9);
            if (ra != null)
            {
                AddFrustum(rk.position, ra.position, 0.046f, 0.030f, 9);
                if (rheel != null && rfi != null) AddFoot(ra.position, rheel.position, rfi.position);
            }
        }

        _mesh.Clear();
        _mesh.SetVertices(_verts);
        _mesh.SetNormals(_normals);
        _mesh.SetTriangles(_tris, 0);
        _mesh.RecalculateBounds();
    }

    // ── Torso — multi-level elliptical mesh ───────────────────────────────────

    private void AddTorso(Vector3 ls, Vector3 rs, Vector3 lh, Vector3 rh,
                          Vector3 right, Vector3 fwd)
    {
        Vector3 shMid  = (ls + rs) * 0.5f;
        Vector3 hipMid = (lh + rh) * 0.5f;

        float sw = (rs - ls).magnitude;   // shoulder width
        float hw = (rh - lh).magnitude;   // hip width

        const int S = 10;         // sides per ring
        const float D = 0.52f;   // front-back depth as fraction of half-width

        // Four rings: pelvis → waist → chest → clavicle  (bottom to top)
        float   pelvisHW  = hw  * 0.54f;
        Vector3 pelvisC   = hipMid;
        var pelvis = MakeEllipseRing(pelvisC, right, fwd, pelvisHW, pelvisHW * D, S);

        float   waistHW   = Mathf.Min(hw, sw) * 0.46f;
        Vector3 waistC    = Vector3.Lerp(hipMid, shMid, 0.32f);
        var waist  = MakeEllipseRing(waistC, right, fwd, waistHW, waistHW * D, S);

        float   chestHW   = sw * 0.53f;
        Vector3 chestC    = Vector3.Lerp(hipMid, shMid, 0.67f);
        var chest  = MakeEllipseRing(chestC, right, fwd, chestHW, chestHW * D, S);

        float   clavHW    = sw * 0.57f;
        Vector3 clavC     = shMid;
        var clav   = MakeEllipseRing(clavC, right, fwd, clavHW, clavHW * D, S);

        ConnectRings(pelvis, waist,  S);
        ConnectRings(waist,  chest,  S);
        ConnectRings(chest,  clav,   S);
        CapRing(pelvis, pelvisC, flip: true);
        CapRing(clav,   clavC,  flip: false);
    }

    // ── Geometry helpers ──────────────────────────────────────────────────────

    private void AddFlatTri(Vector3 a, Vector3 b, Vector3 c)
    {
        Vector3 n = Vector3.Cross(b - a, c - a).normalized;
        int i = _verts.Count;
        _verts.Add(a); _normals.Add(n);
        _verts.Add(b); _normals.Add(n);
        _verts.Add(c); _normals.Add(n);
        _tris.Add(i); _tris.Add(i + 1); _tris.Add(i + 2);
    }

    private void AddFrustum(Vector3 from, Vector3 to, float r1, float r2, int sides)
    {
        Vector3 axis = to - from;
        if (axis.sqrMagnitude < 0.00001f) return;
        axis.Normalize();

        Vector3 p = Mathf.Abs(Vector3.Dot(axis, Vector3.up)) < 0.9f
            ? Vector3.Cross(axis, Vector3.up).normalized
            : Vector3.Cross(axis, Vector3.right).normalized;
        Vector3 q = Vector3.Cross(axis, p).normalized;

        var ring1 = new Vector3[sides];
        var ring2 = new Vector3[sides];
        for (int i = 0; i < sides; i++)
        {
            float a = i * Mathf.PI * 2f / sides;
            float c = Mathf.Cos(a), s = Mathf.Sin(a);
            ring1[i] = from + (p * c + q * s) * r1;
            ring2[i] = to   + (p * c + q * s) * r2;
        }
        ConnectRings(ring1, ring2, sides);
    }

    private static Vector3[] MakeEllipseRing(Vector3 center, Vector3 right, Vector3 fwd,
                                             float hw, float hd, int sides)
    {
        var ring = new Vector3[sides];
        for (int i = 0; i < sides; i++)
        {
            float a = i * Mathf.PI * 2f / sides;
            ring[i] = center + right * (hw * Mathf.Cos(a)) + fwd * (hd * Mathf.Sin(a));
        }
        return ring;
    }

    private void ConnectRings(Vector3[] r1, Vector3[] r2, int sides)
    {
        for (int i = 0; i < sides; i++)
        {
            int j = (i + 1) % sides;
            AddFlatTri(r1[i], r2[i], r1[j]);
            AddFlatTri(r1[j], r2[i], r2[j]);
        }
    }

    private void CapRing(Vector3[] ring, Vector3 center, bool flip)
    {
        for (int i = 0; i < ring.Length; i++)
        {
            int j = (i + 1) % ring.Length;
            if (flip) AddFlatTri(ring[i], center, ring[j]);
            else      AddFlatTri(ring[j], center, ring[i]);
        }
    }

    private void AddSphere(Vector3 center, float radius, int lat, int lon)
    {
        var pts = new Vector3[lat + 1, lon];
        for (int i = 0; i <= lat; i++)
        {
            float phi = Mathf.PI * i / lat - Mathf.PI * 0.5f;
            float y = Mathf.Sin(phi) * radius, r = Mathf.Cos(phi) * radius;
            for (int j = 0; j < lon; j++)
            {
                float th = j * Mathf.PI * 2f / lon;
                pts[i, j] = center + new Vector3(r * Mathf.Cos(th), y, r * Mathf.Sin(th));
            }
        }
        for (int j = 0; j < lon; j++)
            AddFlatTri(pts[0, j], pts[1, j], pts[1, (j + 1) % lon]);
        for (int i = 1; i < lat - 1; i++)
            for (int j = 0; j < lon; j++)
            {
                int jn = (j + 1) % lon;
                AddFlatTri(pts[i, j],  pts[i + 1, j],  pts[i, jn]);
                AddFlatTri(pts[i, jn], pts[i + 1, j],  pts[i + 1, jn]);
            }
        for (int j = 0; j < lon; j++)
            AddFlatTri(pts[lat - 1, j], pts[lat, j], pts[lat - 1, (j + 1) % lon]);
    }

    private void AddHand(Vector3 wrist, Vector3 elbow, float radius)
    {
        // Small rounded disk — featureless hand stub
        Vector3 axis = (wrist - elbow).normalized;
        Vector3 p = Mathf.Abs(Vector3.Dot(axis, Vector3.up)) < 0.9f
            ? Vector3.Cross(axis, Vector3.up).normalized
            : Vector3.Cross(axis, Vector3.right).normalized;
        Vector3 q    = Vector3.Cross(axis, p).normalized;
        Vector3 tip  = wrist + axis * 0.055f;
        const int S  = 7;

        var base_ = new Vector3[S];
        for (int i = 0; i < S; i++)
        {
            float a = i * Mathf.PI * 2f / S;
            base_[i] = wrist + (p * Mathf.Cos(a) + q * Mathf.Sin(a)) * radius;
        }
        // Side cone to tip
        for (int i = 0; i < S; i++)
        {
            int j = (i + 1) % S;
            AddFlatTri(base_[i], tip, base_[j]);
        }
        // Back disk
        CapRing(base_, wrist, flip: true);
    }

    private void AddFoot(Vector3 ankle, Vector3 heel, Vector3 footIndex)
    {
        Vector3 fwdFoot = (footIndex - heel);
        if (fwdFoot.sqrMagnitude < 0.0001f) return;
        fwdFoot.Normalize();

        Vector3 side = Vector3.Cross(fwdFoot, Vector3.up).normalized;
        float heelW  = 0.046f, toeW = 0.040f, h = 0.032f;

        Vector3 hl = heel      - side * heelW, hr = heel      + side * heelW;
        Vector3 fl = footIndex - side * toeW,  fr = footIndex + side * toeW;
        Vector3 up = Vector3.up * h;

        // Top
        AddFlatTri(hl + up, fr + up, hr + up); AddFlatTri(hl + up, fl + up, fr + up);
        // Bottom
        AddFlatTri(hr, fr, hl); AddFlatTri(hl, fr, fl);
        // Front
        AddFlatTri(fl + up, fr + up, fr); AddFlatTri(fl + up, fr, fl);
        // Back
        AddFlatTri(hr + up, hl + up, hl); AddFlatTri(hr + up, hl, hr);
        // Left side
        AddFlatTri(hl + up, hl, fl); AddFlatTri(hl + up, fl, fl + up);
        // Right side
        AddFlatTri(hr, hr + up, fr + up); AddFlatTri(hr, fr + up, fr);
    }

    // ── Floor grid ────────────────────────────────────────────────────────────

    private void CreateFloorGrid(Shader shader)
    {
        const int tiles = 8; const float tileSize = 0.5f;
        var tex = new Texture2D(tiles, tiles, TextureFormat.RGB24, false) { filterMode = FilterMode.Point };
        for (int x = 0; x < tiles; x++)
            for (int z = 0; z < tiles; z++)
                tex.SetPixel(x, z, (x + z) % 2 == 0 ? Color.white : Color.black);
        tex.Apply();

        var go = GameObject.CreatePrimitive(PrimitiveType.Plane);
        go.name = "FloorGrid";
        Destroy(go.GetComponent<Collider>());
        go.transform.localScale = Vector3.one * (tiles * tileSize / 10f);

        var mat = new Material(shader);
        mat.color = Color.white;
        mat.SetFloat("_Smoothness", 0f);
        mat.SetTexture("_BaseMap", tex);
        go.GetComponent<MeshRenderer>().sharedMaterial = mat;
    }
}

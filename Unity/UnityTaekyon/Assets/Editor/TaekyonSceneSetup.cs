using UnityEngine;
using UnityEditor;
using UnityEditor.SceneManagement;

public static class TaekyonSceneSetup
{
    [MenuItem("Taekyon/Setup Scene")]
    public static void SetupScene()
    {
        // ── MotionSystem GameObject ──────────────────────────────────────────
        var go = GameObject.Find("MotionSystem") ?? new GameObject("MotionSystem");

        AddIfMissing<MotionPlayer>(go);
        AddIfMissing<SkeletonMapper>(go);
        AddIfMissing<DebugSkeletonRenderer>(go);
        var timeCtrl = AddIfMissing<MotionTimeController>(go);

        // ── Wire sample clip ─────────────────────────────────────────────────
        var clip = AssetDatabase.LoadAssetAtPath<TextAsset>(
            "Assets/SampleMotions/test_motion.json");

        if (clip == null)
        {
            Debug.LogError("Taekyon Setup: test_motion.json not found at " +
                           "Assets/SampleMotions/test_motion.json");
        }
        else
        {
            var so = new SerializedObject(timeCtrl);
            so.FindProperty("sampleClip").objectReferenceValue = clip;
            so.ApplyModifiedProperties();
        }

        // ── AndroidBridge GameObject ─────────────────────────────────────────
        // Must be named "AndroidBridge" — Android calls UnitySendMessage with this name.
        var bridge = GameObject.Find("AndroidBridge") ?? new GameObject("AndroidBridge");
        AddIfMissing<AndroidBridge>(bridge);

        // ── Camera: face the skeleton from the front ─────────────────────────
        var cam = Camera.main;
        if (cam != null)
        {
            Undo.RecordObject(cam.transform, "Taekyon camera setup");
            cam.transform.position = new Vector3(0f, 0.9f, -3f);
            cam.transform.rotation = Quaternion.identity;
        }

        // ── Finish ───────────────────────────────────────────────────────────
        EditorSceneManager.MarkSceneDirty(
            UnityEngine.SceneManagement.SceneManager.GetActiveScene());

        Selection.activeGameObject = go;
        Debug.Log("Taekyon: scene ready. Press Play.");
    }

    private static T AddIfMissing<T>(GameObject go) where T : Component
    {
        var c = go.GetComponent<T>();
        return c != null ? c : go.AddComponent<T>();
    }
}

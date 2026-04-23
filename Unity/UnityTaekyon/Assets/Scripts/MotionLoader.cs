using System;
using System.Collections.Generic;
using UnityEngine;

// Custom JSON parser for MotionClip. Not using JsonUtility because it does not
// support Dictionary<string, Vector3> natively.
public static class MotionLoader
{
    private static string _src;
    private static int _pos;

    public static MotionClip Load(string json)
    {
        _src = json;
        _pos = 0;

        var root = ParseObject();
        var clip = new MotionClip();

        if (root.TryGetValue("fps", out var fpsVal))
            clip.fps = Convert.ToInt32(fpsVal);

        if (root.TryGetValue("frames", out var framesVal))
        {
            var list = (List<object>)framesVal;
            clip.frames = new MotionFrame[list.Count];
            for (int i = 0; i < list.Count; i++)
                clip.frames[i] = ParseMotionFrame((Dictionary<string, object>)list[i]);
        }

        return clip;
    }

    // ── Internal parser ─────────────────────────────────────────────────────

    private static MotionFrame ParseMotionFrame(Dictionary<string, object> obj)
    {
        var frame = new MotionFrame();
        if (!obj.TryGetValue("joints", out var jointsVal)) return frame;

        foreach (var kv in (Dictionary<string, object>)jointsVal)
        {
            var arr = (List<object>)kv.Value;
            frame.joints[kv.Key] = new Vector3(
                Convert.ToSingle(arr[0]),
                Convert.ToSingle(arr[1]),
                Convert.ToSingle(arr[2]));
        }
        return frame;
    }

    private static object ParseValue()
    {
        SkipWhitespace();
        if (_pos >= _src.Length) return null;

        char c = _src[_pos];
        if (c == '{') return ParseObject();
        if (c == '[') return ParseArray();
        if (c == '"') return ParseString();
        if (c == 't') { _pos += 4; return true; }
        if (c == 'f') { _pos += 5; return false; }
        if (c == 'n') { _pos += 4; return null; }
        return ParseNumber();
    }

    private static Dictionary<string, object> ParseObject()
    {
        Expect('{');
        var dict = new Dictionary<string, object>();
        SkipWhitespace();
        if (Peek() == '}') { _pos++; return dict; }

        while (true)
        {
            SkipWhitespace();
            string key = ParseString();
            SkipWhitespace();
            Expect(':');
            object value = ParseValue();
            dict[key] = value;
            SkipWhitespace();
            if (Peek() == '}') { _pos++; break; }
            Expect(',');
        }
        return dict;
    }

    private static List<object> ParseArray()
    {
        Expect('[');
        var list = new List<object>();
        SkipWhitespace();
        if (Peek() == ']') { _pos++; return list; }

        while (true)
        {
            list.Add(ParseValue());
            SkipWhitespace();
            if (Peek() == ']') { _pos++; break; }
            Expect(',');
        }
        return list;
    }

    private static string ParseString()
    {
        Expect('"');
        int start = _pos;
        while (_pos < _src.Length && _src[_pos] != '"') _pos++;
        string result = _src.Substring(start, _pos - start);
        Expect('"');
        return result;
    }

    private static double ParseNumber()
    {
        int start = _pos;
        while (_pos < _src.Length && "-+0123456789.eE".IndexOf(_src[_pos]) >= 0) _pos++;
        return double.Parse(_src.Substring(start, _pos - start),
                            System.Globalization.CultureInfo.InvariantCulture);
    }

    private static void SkipWhitespace()
    {
        while (_pos < _src.Length && (_src[_pos] == ' ' || _src[_pos] == '\t' ||
               _src[_pos] == '\r' || _src[_pos] == '\n'))
            _pos++;
    }

    private static char Peek() => _pos < _src.Length ? _src[_pos] : '\0';

    private static void Expect(char c)
    {
        if (_pos >= _src.Length || _src[_pos] != c)
            throw new Exception($"MotionLoader: expected '{c}' at pos {_pos}, got '{Peek()}'");
        _pos++;
    }
}

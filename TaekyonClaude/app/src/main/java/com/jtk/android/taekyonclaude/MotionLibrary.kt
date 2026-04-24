package com.jtk.android.taekyonclaude

import android.content.Context

object MotionLibrary {

    fun listClipNames(context: Context): List<String> =
        context.assets.list("motions")
            ?.filter { it.endsWith(".json") && it.startsWith("kick_") }
            ?.map { it.removeSuffix(".json") }
            ?.sorted()
            ?: emptyList()

    fun loadJson(context: Context, clipName: String): String =
        context.assets.open("motions/$clipName.json").bufferedReader().readText()
}

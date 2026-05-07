package com.jtk.android.taekyonclaude

import android.content.Context

data class MoveType(
    val id: String,          // matches MoveVariant.moveType in Unity Inspector
    val displayName: String,
)

object MotionLibrary {

    // Add new move types here as clips are captured and added to Unity.
    // Each id must match the {move_type} prefix in the clip filename convention:
    //   {move_type}_{stance}_{leg_role}_{height}.json
    val moveTypes: List<MoveType> = listOf(
        MoveType("roundhouse_low",  "Low Roundhouse Kick"),
        MoveType("roundhouse_high", "High Roundhouse Kick"),  // clips not yet captured
        MoveType("split_kick_low",  "Low Split Kick"),        // clips not yet captured
    )

    fun loadJson(context: Context, clipName: String): String =
        context.assets.open("motions/$clipName.json").bufferedReader().readText()
}

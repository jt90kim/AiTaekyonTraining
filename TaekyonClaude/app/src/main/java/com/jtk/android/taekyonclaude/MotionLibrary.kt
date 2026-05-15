package com.jtk.android.taekyonclaude

enum class Status { Ready, Soon }

data class HeightVariant(
    val id: String,
    val label: String,
    val variants: Int,
    val status: Status,
)

data class TechniqueFamily(
    val id: String,
    val name: String,
    val hangul: String,
    val romaja: String,
    val desc: String,
    val status: Status,
    val heights: List<HeightVariant>,
)

object MotionLibrary {

    val techniques: List<TechniqueFamily> = listOf(
        TechniqueFamily(
            id = "roundhouse", name = "Roundhouse",
            hangul = "돌려차기", romaja = "Dollyeo-chagi",
            desc = "Circular sweep with the shin · whole-body rotation",
            status = Status.Ready,
            heights = listOf(
                HeightVariant("roundhouse_low",  "Low",  4, Status.Ready),
                HeightVariant("roundhouse_high", "High", 4, Status.Soon),
            ),
        ),
        TechniqueFamily(
            id = "front", name = "Front Kick",
            hangul = "앞차기", romaja = "Ap-chagi",
            desc = "Straight thrust off the lead leg",
            status = Status.Soon,
            heights = listOf(
                HeightVariant("front_low",  "Low",  4, Status.Soon),
                HeightVariant("front_high", "High", 4, Status.Soon),
            ),
        ),
        TechniqueFamily(
            id = "side", name = "Side Kick",
            hangul = "옆차기", romaja = "Yeop-chagi",
            desc = "Linear push from the hip · long range",
            status = Status.Soon,
            heights = listOf(
                HeightVariant("side_low",  "Low",  4, Status.Soon),
                HeightVariant("side_high", "High", 4, Status.Soon),
            ),
        ),
        TechniqueFamily(
            id = "hook", name = "Hook Kick",
            hangul = "후려차기", romaja = "Huryeo-chagi",
            desc = "Whipping return — heel as the striking surface",
            status = Status.Soon,
            heights = listOf(
                HeightVariant("hook_low",  "Low",  4, Status.Soon),
                HeightVariant("hook_high", "High", 4, Status.Soon),
            ),
        ),
    )

    val durationPresets = listOf(30, 60, 120, 180, 300)

    fun fmtMSS(totalSeconds: Int): String {
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return "$m:${s.toString().padStart(2, '0')}"
    }
}

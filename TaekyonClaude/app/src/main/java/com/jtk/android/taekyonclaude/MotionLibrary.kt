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
            id = "split", name = "Splint Kick",
            hangul = "내차기", romaja = "Nae-chagi",
            desc = "Inward sweeping arc — shin travels across the centerline",
            status = Status.Soon,
            heights = listOf(
                HeightVariant("split_low", "Low", 4, Status.Soon),
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

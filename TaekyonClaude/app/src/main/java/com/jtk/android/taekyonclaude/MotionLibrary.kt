package com.jtk.android.taekyonclaude

enum class Status { Ready, Soon }

data class HeightVariant(
    val id: String,
    val label: String,
    val variants: Int,
    val status: Status,
    val hangul: String? = null,
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
            hangul = "후려차기 / 돌려차기", romaja = "Huryeo-chagi / Dollyeo-chagi",
            desc = "Circular sweep with the shin · whole-body rotation",
            status = Status.Ready,
            heights = listOf(
                HeightVariant("roundhouse_low",  "Low",  4, Status.Ready),
                HeightVariant("roundhouse_high", "High", 4, Status.Soon),
            ),
        ),
        TechniqueFamily(
            id = "splint", name = "Splint Kick",
            hangul = "내차기", romaja = "Nae-chagi",
            desc = "Inward sweeping arc — shin travels across the centerline",
            status = Status.Ready,
            heights = listOf(
                HeightVariant("splint_low",  "Low",  4, Status.Ready),
                HeightVariant("splint_high", "High", 4, Status.Ready, hangul = "곁차기 / 높은내차기"),
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

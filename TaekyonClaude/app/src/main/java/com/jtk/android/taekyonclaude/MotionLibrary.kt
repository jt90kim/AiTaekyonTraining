package com.jtk.android.taekyonclaude

import androidx.annotation.StringRes

enum class Status { Ready, Soon }

data class HeightVariant(
    val id: String,
    @StringRes val labelResId: Int,
    val variants: Int,
    val status: Status,
    val hangul: String? = null,
)

data class TechniqueFamily(
    val id: String,
    @StringRes val nameResId: Int,
    val hangul: String,
    val romaja: String,
    @StringRes val descResId: Int,
    val status: Status,
    val heights: List<HeightVariant>,
)

object MotionLibrary {

    val techniques: List<TechniqueFamily> = listOf(
        TechniqueFamily(
            id = "roundhouse",
            nameResId = R.string.technique_roundhouse,
            hangul = "후려차기 / 돌려차기",
            romaja = "Huryeo-chagi / Dollyeo-chagi",
            descResId = R.string.technique_roundhouse_desc,
            status = Status.Ready,
            heights = listOf(
                HeightVariant("roundhouse_low",  R.string.height_low,  4, Status.Ready),
                HeightVariant("roundhouse_high", R.string.height_high, 4, Status.Ready),
            ),
        ),
        TechniqueFamily(
            id = "splint",
            nameResId = R.string.technique_splint,
            hangul = "내차기",
            romaja = "Nae-chagi",
            descResId = R.string.technique_splint_desc,
            status = Status.Ready,
            heights = listOf(
                HeightVariant("splint_low",  R.string.height_low,  4, Status.Ready),
                HeightVariant("splint_high", R.string.height_high, 4, Status.Ready, hangul = "곁차기 / 높은내차기"),
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

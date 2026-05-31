package com.taekyun.flow

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionLibraryTest {

    // ── fmtMSS ──────────────────────────────────────────────────────────────

    @Test fun fmtMSS_zero() = assertEquals("0:00", MotionLibrary.fmtMSS(0))

    @Test fun fmtMSS_singleDigitSeconds_zeroPadded() = assertEquals("0:09", MotionLibrary.fmtMSS(9))

    @Test fun fmtMSS_30s() = assertEquals("0:30", MotionLibrary.fmtMSS(30))

    @Test fun fmtMSS_exactMinute() = assertEquals("1:00", MotionLibrary.fmtMSS(60))

    @Test fun fmtMSS_minuteAndSingleDigit_zeroPadded() = assertEquals("1:05", MotionLibrary.fmtMSS(65))

    @Test fun fmtMSS_minuteAndHalf() = assertEquals("1:30", MotionLibrary.fmtMSS(90))

    @Test fun fmtMSS_fiveMinutes() = assertEquals("5:00", MotionLibrary.fmtMSS(300))

    // ── durationPresets ─────────────────────────────────────────────────────

    @Test fun durationPresets_exactValues() {
        assertEquals(listOf(30, 60, 120, 180, 300), MotionLibrary.durationPresets)
    }

    @Test fun durationPresets_minIs30s() {
        assertEquals(30, MotionLibrary.durationPresets.min())
    }

    @Test fun durationPresets_maxIs300s() {
        assertEquals(300, MotionLibrary.durationPresets.max())
    }

    // ── techniques structure ─────────────────────────────────────────────────

    @Test fun techniques_countIsTwo() {
        assertEquals(2, MotionLibrary.techniques.size)
    }

    @Test fun techniques_allFamiliesAreReady() {
        assertTrue(MotionLibrary.techniques.all { it.status == Status.Ready })
    }

    @Test fun techniques_eachFamilyHasTwoHeightVariants() {
        MotionLibrary.techniques.forEach { family ->
            assertEquals("${family.id} should have 2 height variants", 2, family.heights.size)
        }
    }

    @Test fun techniques_allHeightVariantsAreReady() {
        val allHeights = MotionLibrary.techniques.flatMap { it.heights }
        assertTrue(allHeights.all { it.status == Status.Ready })
    }

    @Test fun techniques_allHeightVariantsHaveFourClips() {
        val allHeights = MotionLibrary.techniques.flatMap { it.heights }
        assertTrue(allHeights.all { it.variants == 4 })
    }

    @Test fun techniques_variantIdsAreCorrect() {
        val ids = MotionLibrary.techniques.flatMap { it.heights }.map { it.id }
        assertEquals(
            listOf("roundhouse_low", "roundhouse_high", "splint_low", "splint_high"),
            ids,
        )
    }

    @Test fun techniques_roundhouseIsFirst() {
        assertEquals("roundhouse", MotionLibrary.techniques.first().id)
    }

    @Test fun techniques_splintIsSecond() {
        assertEquals("splint", MotionLibrary.techniques[1].id)
    }
}

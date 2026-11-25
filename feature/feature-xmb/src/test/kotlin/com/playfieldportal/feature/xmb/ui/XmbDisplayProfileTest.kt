package com.playfieldportal.feature.xmb.ui

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The 4:3 auto-detection and the row profile it selects (see XmbDisplayProfile). The RG Slide's
 * 731 × 549 dp canvas is the reference 4:3 screen; the 16:9 handheld baseline (832 × 468 dp) must
 * stay on the standard profile so every existing layout test keeps describing it.
 */
class XmbDisplayProfileTest {

    @Test
    fun `4-to-3 screens are detected, 16-to-9 and wider are not`() {
        // The RG Slide: 1280 × 960 at 280 dpi.
        assertTrue(XmbDisplayProfile.isFourThirds(731f, 549f))
        // Exactly 4:3.
        assertTrue(XmbDisplayProfile.isFourThirds(1024f, 768f))
        // The 16:9 handheld baseline (the Thor at 1920 × 1080).
        assertFalse(XmbDisplayProfile.isFourThirds(832f, 468f))
        // A 16:9 phone canvas.
        assertFalse(XmbDisplayProfile.isFourThirds(891f, 411f))
        // 3:2 is the widest "tall" candidate and stays standard.
        assertFalse(XmbDisplayProfile.isFourThirds(750f, 500f))
        // Degenerate inputs never select the tall profile.
        assertFalse(XmbDisplayProfile.isFourThirds(0f, 0f))
        assertFalse(XmbDisplayProfile.isFourThirds(731f, 0f))
    }

    @Test
    fun `the standard profile is the 16-to-9 constants, verbatim`() {
        val standard = XmbRowProfile.STANDARD
        assertEquals(GAME_ICON_WIDTH, standard.gameIconWidth)
        assertEquals(GAME_ICON_HEIGHT, standard.gameIconHeight)
        // The video snap fills the static tile exactly, as it always has on 16:9.
        assertEquals(GAME_ICON_WIDTH, standard.videoIconWidth)
        assertEquals(GAME_ICON_HEIGHT, standard.videoIconHeight)
        assertEquals(ROW_HEIGHT, standard.rowHeight)
        assertEquals(UMD_ICON_WIDTH, standard.umdIconWidth)
        assertEquals(UMD_ICON_HEIGHT, standard.umdIconHeight)
        assertEquals(UMD_ROW_HEIGHT, standard.umdRowHeight)
        assertEquals(TAP_TARGET_HEIGHT, standard.tapTargetHeight)
        assertEquals(ARTWORK_TEXT_GAP, standard.artworkTextGap)
        assertEquals(NATURAL_ART_HEIGHT, standard.naturalArtHeight)
        // XMBShell's PIC0 logo box, as drawn before the profile existed.
        assertEquals(0.30f, standard.logoWidthFraction)
        assertEquals(0.38f, standard.logoHeightFraction)
    }

    @Test
    fun `the tall profile grows the layout and unifies the game art`() {
        val factor = 4f / 3f
        val tall = XmbRowProfile.FOUR_THIRDS
        assertEquals(XmbRowProfile.STANDARD.umdRowHeight * factor, tall.umdRowHeight)
        assertEquals(XmbRowProfile.STANDARD.naturalArtHeight * factor, tall.naturalArtHeight)
        assertEquals(XmbRowProfile.STANDARD.logoWidthFraction * factor, tall.logoWidthFraction)
        assertEquals(XmbRowProfile.STANDARD.logoHeightFraction * factor, tall.logoHeightFraction)
        // ONE box for the static cover and the video snap: the fade between them never resizes.
        assertEquals(168.dp, tall.gameIconWidth)
        assertEquals(112.dp, tall.gameIconHeight)
        assertEquals(tall.gameIconWidth, tall.videoIconWidth)
        assertEquals(tall.gameIconHeight, tall.videoIconHeight)
        // The box sits on the row's art budget (the box-art line).
        assertEquals(tall.naturalArtHeight, tall.gameIconHeight)
        // The pitch keeps the design's own breathing ratio (16:9: 70 in 88 = 1.26): the bigger
        // art reads as PSP rows, not a wall of tiles, and never exceeds the pitch.
        assertEquals(128.dp, tall.rowHeight)
        assertTrue(tall.gameIconHeight < tall.rowHeight)
        assertTrue(tall.rowHeight / tall.gameIconHeight > 1.13f)
    }
}

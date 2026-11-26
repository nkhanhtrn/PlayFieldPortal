package com.playfieldportal.feature.xmb.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── 4:3 auto-detection, and the row/logo profile it selects ──────────────────
//
// The XMB's tuned geometry (XMBItemList's row constants, XMBShell's PIC0 logo box) was drawn for
// the 16:9 handheld canvas (XMB_BASELINE 832 × 468 dp). A 4:3 screen (the RG Slide: 1280 × 960 at
// 280 dpi = 731 × 549 dp) is width-poor and height-rich: the shell's canvas scale stays pinned at
// 1.0 (its width ratio binds and clamps to the handheld minimum), and the extra vertical dp render
// as emptiness around unchanged rows — the game list reads small. On a 4:3 screen only, the
// selected profile grows the game art, the video snap slot and the PIC0 logo by the screen's own
// ratio (4/3). Text, caticons, the category bar and every other screen keep their sizes.
//
// [XmbDisplayProfile.ENABLED] is the master switch for every 4:3-only layout change: flip it to
// false and a 4:3 device renders exactly the 16:9 layout again, whatever else landed since. New
// 4:3 adjustments belong in [XmbRowProfile] — one data class, provided through
// [LocalXmbRowProfile] at the shell root and read ambiently by the layout, so a future tweak is
// one field, not a scattering of if (isFourThirds) sites.

object XmbDisplayProfile {
    /** Master switch for the whole 4:3 layout; false = every 4:3 screen renders the 16:9 layout. */
    const val ENABLED = true

    /** 4:3 root balance: the crossbar sits this much lower than the tuned fraction, landing the
     *  selected row's centre on the screen's midline (the tuned 16:9 geometry parks it ~2.5%
     *  high, which reads as top-heavy once the taller rows fill the canvas). Applies wherever
     *  the crossbar line is computed, so the bar and the drill flyout's logo anchor cannot drift. */
    const val ROOT_BAR_TOP_BIAS = 0.016f

    /** 4:3 is 1.333; 3:2 (1.5) and wider stay standard so phones and tablets are untouched. */
    const val FOUR_THIRDS_MAX_ASPECT = 1.45f

    /** True for landscape screens at or under the 4:3-ish aspect. */
    fun isFourThirds(widthDp: Float, heightDp: Float): Boolean =
        widthDp > 0f && heightDp > 0f && widthDp / heightDp <= FOUR_THIRDS_MAX_ASPECT
}

/**
 * One aspect's worth of XMB row geometry: the values XMBItemList lays the item rows out with,
 * built from its standard constants so the two can never drift, plus the PIC0 logo box fractions
 * XMBShell draws. Read ambiently via [LocalXmbRowProfile].
 */
@Immutable
data class XmbRowProfile(
    val gameIconWidth: Dp,
    val gameIconHeight: Dp,
    /** The focused game's ICON1 video snap: rendered over the static tile, sized independently. */
    val videoIconWidth: Dp,
    val videoIconHeight: Dp,
    val rowHeight: Dp,
    val umdIconWidth: Dp,
    val umdIconHeight: Dp,
    val umdRowHeight: Dp,
    val tapTargetHeight: Dp,
    val artworkTextGap: Dp,
    val naturalArtHeight: Dp,
    val logoWidthFraction: Float,
    val logoHeightFraction: Float,
) {
    /** Every dimension times [factor] — the logo fractions too, they are the same canvas. */
    fun scaledBy(factor: Float): XmbRowProfile = copy(
        gameIconWidth = gameIconWidth * factor,
        gameIconHeight = gameIconHeight * factor,
        videoIconWidth = videoIconWidth * factor,
        videoIconHeight = videoIconHeight * factor,
        rowHeight = rowHeight * factor,
        umdIconWidth = umdIconWidth * factor,
        umdIconHeight = umdIconHeight * factor,
        umdRowHeight = umdRowHeight * factor,
        tapTargetHeight = tapTargetHeight * factor,
        artworkTextGap = artworkTextGap * factor,
        naturalArtHeight = naturalArtHeight * factor,
        logoWidthFraction = logoWidthFraction * factor,
        logoHeightFraction = logoHeightFraction * factor,
    )

    companion object {
        /** The 16:9 handheld layout — XMBItemList's tuned constants, verbatim. The video snap
         *  fills the static tile exactly, as it always has on 16:9. */
        val STANDARD = XmbRowProfile(
            gameIconWidth = GAME_ICON_WIDTH,
            gameIconHeight = GAME_ICON_HEIGHT,
            videoIconWidth = GAME_ICON_WIDTH,
            videoIconHeight = GAME_ICON_HEIGHT,
            rowHeight = ROW_HEIGHT,
            umdIconWidth = UMD_ICON_WIDTH,
            umdIconHeight = UMD_ICON_HEIGHT,
            umdRowHeight = UMD_ROW_HEIGHT,
            tapTargetHeight = TAP_TARGET_HEIGHT,
            artworkTextGap = ARTWORK_TEXT_GAP,
            naturalArtHeight = NATURAL_ART_HEIGHT,
            logoWidthFraction = 0.30f,
            logoHeightFraction = 0.38f,
        )

        /** The 4:3 layout: the standard one grown by the screen's own ratio (4/3), then the
         *  game art unified — the static tile and the focused game's video snap render in ONE box
         *  (the video's: 168 × 112), so the snap playing over its poster and the fade back to it
         *  are pixel-identical, and every row's cover reads at the same size as the snap beside
         *  it. The height is the row's full art budget (the box-art line): 4:3 screens are
         *  height-rich, so the art takes the space. The pitch stays on the design's own rhythm
         *  (16:9 is 70 art in an 88 row = 1.26; 112 in 128 here), so the bigger art keeps the
         *  PSP's breathing room instead of reading as a wall of tiles. (Crop scales both the 1.8
         *  ICON0 art and the 4:3 snaps into the 1.5 box, the way the PSP cropped ICON1 into the
         *  1.8 tile — one box, both artifacts.) */
        val FOUR_THIRDS = STANDARD.scaledBy(4f / 3f).copy(
            gameIconWidth = 168.dp,
            gameIconHeight = 112.dp,
            videoIconWidth = 168.dp,
            videoIconHeight = 112.dp,
            rowHeight = 128.dp,
        )
    }
}

/** The profile in effect; [XmbRowProfile.STANDARD] everywhere outside the shell (tests, previews). */
val LocalXmbRowProfile = staticCompositionLocalOf { XmbRowProfile.STANDARD }

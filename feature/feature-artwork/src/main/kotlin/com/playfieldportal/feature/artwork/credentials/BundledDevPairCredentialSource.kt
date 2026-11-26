package com.playfieldportal.feature.artwork.credentials

import com.playfieldportal.feature.artwork.BuildConfig
import com.playfieldportal.feature.artwork.MetadataApiKeyProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reassembles the ScreenScraper developer pair that the Gradle build XOR-encoded into
 * `BuildConfig` (see feature-artwork/build.gradle.kts). Pure so the round-trip is unit-testable.
 *
 * This is obfuscation, not encryption: the key material travels with the APK, so a decompiler
 * recovers the pair. It defeats `strings` scrapes and automated harvesters only.
 */
object DevPairDecoder {

    /**
     * XORs [share] with [mask] and decodes the result as UTF-8. Either array empty (or a length
     * mismatch, which cannot happen when the Gradle generator produced both) yields null — the
     * bundled pair simply does not exist in that build.
     */
    fun decode(share: ByteArray?, mask: ByteArray?): String? {
        if (share == null || mask == null) return null
        if (share.isEmpty() || mask.isEmpty() || share.size != mask.size) return null
        val plain = ByteArray(share.size) { i -> (share[i].toInt() xor mask[i].toInt()).toByte() }
        val text = String(plain, Charsets.UTF_8)
        return text.trim().takeIf { it.isNotEmpty() }
    }
}

/**
 * The build's developer pair, reassembled from the XOR-encoded `BuildConfig` arrays. Used by
 * [RuntimeDevPairCredentialSource] as the fallback when the user never saved a pair of their own.
 *
 * ScreenScraper validates the pair as a unit, so there is deliberately no code path that can mix
 * a stored half with a bundled half.
 */
@Singleton
class BundledDevPairCredentialSource @Inject constructor(
    private val keys: MetadataApiKeyProvider,
) {

    /** The bundled pair, or null when this build compiled without one. */
    fun pairOrNull(): Pair<String, String>? {
        val devId = DevPairDecoder.decode(BuildConfig.SS_DEV_ID_SHARE, BuildConfig.SS_DEV_ID_MASK)
        val devPassword = DevPairDecoder.decode(BuildConfig.SS_DEV_PASSWORD_SHARE, BuildConfig.SS_DEV_PASSWORD_MASK)
        return if (devId != null && devPassword != null) devId to devPassword else null
    }

    /** True when this build compiled a pair in — Settings uses it to decide whether to offer entry. */
    val bundledPairPresent: Boolean get() = pairOrNull() != null
}

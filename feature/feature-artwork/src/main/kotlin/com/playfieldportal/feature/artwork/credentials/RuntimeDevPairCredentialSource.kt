package com.playfieldportal.feature.artwork.credentials

import com.playfieldportal.feature.artwork.MetadataApiKeyProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The credential source that decides which ScreenScraper developer pair is in force.
 *
 * Order: a pair the user saved in Settings ▸ Artwork wins; without one, the build's bundled pair
 * ([BundledDevPairCredentialSource]) is used; with neither, ScreenScraper is off entirely (null).
 * The optional user account (username/password) rides along with whichever pair won, so saving a
 * pair needs no re-login.
 *
 * ScreenScraper validates a pair as a unit, so a stored half pair is never mixed with the bundled
 * half — [ScreenScraperCredentials.of] enforces that both halves come from the same source here.
 * This is the fallback-shaped design [MetadataCredentialSource] was written for: a build that
 * ships no pair (this fork) is unlocked by typing one in, not by rebuilding.
 */
@Singleton
class RuntimeDevPairCredentialSource @Inject constructor(
    private val keys: MetadataApiKeyProvider,
    private val bundled: BundledDevPairCredentialSource,
) : MetadataCredentialSource {

    /** The stored pair, or null when the user never finished entering one. */
    private suspend fun storedPair(): Pair<String, String>? =
        storedPairOf(keys.getSsDevId(), keys.getSsDevPassword())

    /** The pair in force: the user's, else the build's. Null = ScreenScraper off. */
    private suspend fun effectivePair(): Pair<String, String>? =
        effectivePairOf(storedPair(), bundled.pairOrNull())

    companion object {
        /** Both halves or nothing: a half-entered pair is not a pair, and is never mixed. */
        fun storedPairOf(devId: String?, devPassword: String?): Pair<String, String>? {
            val id = devId?.trim().orEmpty()
            val password = devPassword?.trim().orEmpty()
            return if (id.isEmpty() || password.isEmpty()) null else id to password
        }

        /** The stored pair wins; the bundled pair is the fallback; neither = off. */
        fun effectivePairOf(
            stored: Pair<String, String>?,
            bundled: Pair<String, String>?,
        ): Pair<String, String>? = stored ?: bundled
    }

    override val screenScraper: Flow<ScreenScraperCredentials?> =
        combine(keys.ssDevIdFlow, keys.ssUsernameFlow) { devId, _ -> devId }
            .map {
                val (devId, devPassword) = effectivePair() ?: return@map null
                ScreenScraperCredentials.of(
                    devId        = devId,
                    devPassword  = devPassword,
                    userId       = keys.getSsUsername(),
                    userPassword = keys.getSsPassword(),
                )
            }

    override suspend fun screenScraperNow(): ScreenScraperCredentials? {
        val (devId, devPassword) = effectivePair() ?: return null
        return ScreenScraperCredentials.of(
            devId        = devId,
            devPassword  = devPassword,
            userId       = keys.getSsUsername(),
            userPassword = keys.getSsPassword(),
        )
    }
}

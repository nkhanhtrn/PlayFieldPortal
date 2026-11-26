package com.playfieldportal.feature.artwork.credentials

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The pair-resolution rules of [RuntimeDevPairCredentialSource], kept pure so they can be tested
 * without a DataStore: both halves or nothing, the stored pair wins, the bundled pair is the
 * fallback.
 */
class RuntimeDevPairCredentialSourceTest {

    @Test
    fun `a stored pair needs both halves`() {
        assertEquals("id" to "pw", RuntimeDevPairCredentialSource.storedPairOf("id", "pw"))
        assertNull(RuntimeDevPairCredentialSource.storedPairOf(null, "pw"))
        assertNull(RuntimeDevPairCredentialSource.storedPairOf("id", null))
        assertNull(RuntimeDevPairCredentialSource.storedPairOf("", "pw"))
        assertNull(RuntimeDevPairCredentialSource.storedPairOf("id", ""))
        // Whitespace-only halves are as empty as missing ones; real halves are trimmed.
        assertNull(RuntimeDevPairCredentialSource.storedPairOf("  ", "pw"))
        assertEquals("id" to "pw", RuntimeDevPairCredentialSource.storedPairOf(" id ", " pw "))
    }

    @Test
    fun `the stored pair overrides the bundled pair`() {
        val bundled = "build" to "pair"
        assertEquals("own" to "pair", RuntimeDevPairCredentialSource.effectivePairOf("own" to "pair", bundled))
        assertEquals(bundled, RuntimeDevPairCredentialSource.effectivePairOf(null, bundled))
        assertNull(RuntimeDevPairCredentialSource.effectivePairOf(null, null))
    }

    @Test
    fun `a half stored pair falls back whole to the bundled pair, never mixes`() {
        val bundled = "build" to "pair"
        // A half-entered stored pair is null by storedPairOf, so the bundled pair serves whole.
        assertEquals(
            bundled,
            RuntimeDevPairCredentialSource.effectivePairOf(
                RuntimeDevPairCredentialSource.storedPairOf("own", null),
                bundled,
            ),
        )
    }
}

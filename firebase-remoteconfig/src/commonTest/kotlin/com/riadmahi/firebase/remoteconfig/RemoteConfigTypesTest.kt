package com.riadmahi.firebase.remoteconfig

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for Firebase Remote Config types to validate API consistency.
 *
 * Reference: https://firebase.google.com/docs/remote-config
 */
class RemoteConfigTypesTest {

    // region RemoteConfigValue - matches Firebase RemoteConfigValue

    @Test
    fun `RemoteConfigValue should have string representation`() {
        val value = RemoteConfigValue(
            asString = "hello",
            asBoolean = false,
            asLong = 0L,
            asDouble = 0.0,
            source = ValueSource.REMOTE
        )

        assertEquals("hello", value.asString)
    }

    @Test
    fun `RemoteConfigValue should have boolean representation`() {
        val value = RemoteConfigValue(
            asString = "true",
            asBoolean = true,
            asLong = 1L,
            asDouble = 1.0,
            source = ValueSource.REMOTE
        )

        assertEquals(true, value.asBoolean)
    }

    @Test
    fun `RemoteConfigValue should have long representation`() {
        val value = RemoteConfigValue(
            asString = "42",
            asBoolean = false,
            asLong = 42L,
            asDouble = 42.0,
            source = ValueSource.REMOTE
        )

        assertEquals(42L, value.asLong)
    }

    @Test
    fun `RemoteConfigValue should have double representation`() {
        val value = RemoteConfigValue(
            asString = "3.14",
            asBoolean = false,
            asLong = 3L,
            asDouble = 3.14,
            source = ValueSource.REMOTE
        )

        assertEquals(3.14, value.asDouble)
    }

    @Test
    fun `RemoteConfigValue should indicate source`() {
        val remoteValue = RemoteConfigValue(
            asString = "remote",
            asBoolean = false,
            asLong = 0L,
            asDouble = 0.0,
            source = ValueSource.REMOTE
        )

        val defaultValue = RemoteConfigValue(
            asString = "default",
            asBoolean = false,
            asLong = 0L,
            asDouble = 0.0,
            source = ValueSource.DEFAULT
        )

        val staticValue = RemoteConfigValue(
            asString = "",
            asBoolean = false,
            asLong = 0L,
            asDouble = 0.0,
            source = ValueSource.STATIC
        )

        assertEquals(ValueSource.REMOTE, remoteValue.source)
        assertEquals(ValueSource.DEFAULT, defaultValue.source)
        assertEquals(ValueSource.STATIC, staticValue.source)
    }

    // endregion

    // region ValueSource - matches Firebase ValueSource

    @Test
    fun `ValueSource should have DEFAULT for app defaults`() {
        assertEquals(ValueSource.DEFAULT, ValueSource.entries[0])
    }

    @Test
    fun `ValueSource should have REMOTE for server values`() {
        assertEquals(ValueSource.REMOTE, ValueSource.entries[1])
    }

    @Test
    fun `ValueSource should have STATIC for type defaults`() {
        assertEquals(ValueSource.STATIC, ValueSource.entries[2])
    }

    @Test
    fun `ValueSource should have exactly 3 values`() {
        assertEquals(3, ValueSource.entries.size)
    }

    // endregion

    // region RemoteConfigException - matches Firebase RemoteConfigException

    @Test
    fun `FetchThrottled should indicate rate limiting`() {
        val exception = RemoteConfigException.FetchThrottled("Too many fetch requests")

        assertIs<RemoteConfigException>(exception)
        assertEquals("Too many fetch requests", exception.message)
    }

    @Test
    fun `FetchFailed should indicate fetch error`() {
        val exception = RemoteConfigException.FetchFailed("Network error during fetch")

        assertIs<RemoteConfigException>(exception)
        assertEquals("Network error during fetch", exception.message)
    }

    @Test
    fun `ActivationFailed should indicate activation error`() {
        val exception = RemoteConfigException.ActivationFailed("Failed to activate config")

        assertIs<RemoteConfigException>(exception)
        assertEquals("Failed to activate config", exception.message)
    }

    @Test
    fun `RemoteConfigException Unknown should be catch-all`() {
        val exception = RemoteConfigException.Unknown("Unexpected error")

        assertIs<RemoteConfigException>(exception)
        assertEquals("Unexpected error", exception.message)
    }

    @Test
    fun `RemoteConfigException should accept cause`() {
        val cause = RuntimeException("Original error")
        val exception = RemoteConfigException.FetchFailed("Fetch failed", cause)

        assertEquals(cause, exception.cause)
    }

    // endregion

    // region when expression exhaustiveness

    @Test
    fun `when expression should be exhaustive for RemoteConfigException`() {
        val exceptions: List<RemoteConfigException> = listOf(
            RemoteConfigException.FetchThrottled("throttled"),
            RemoteConfigException.FetchFailed("failed"),
            RemoteConfigException.ActivationFailed("activation failed"),
            RemoteConfigException.Unknown("unknown")
        )

        exceptions.forEach { exception ->
            val handled = when (exception) {
                is RemoteConfigException.FetchThrottled -> true
                is RemoteConfigException.FetchFailed -> true
                is RemoteConfigException.ActivationFailed -> true
                is RemoteConfigException.Unknown -> true
            }
            assertEquals(true, handled)
        }

        assertEquals(4, exceptions.size)
    }

    // endregion
}

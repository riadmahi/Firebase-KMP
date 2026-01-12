package com.riadmahi.firebase.firestore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/**
 * Tests for Firestore data types to validate API consistency with Firebase Firestore.
 *
 * Reference: https://firebase.google.com/docs/firestore/manage-data/data-types
 */
class FirestoreTypesTest {

    // region GeoPoint - matches Firebase GeoPoint

    @Test
    fun `GeoPoint should store latitude and longitude`() {
        val geoPoint = GeoPoint(
            latitude = 37.7749,
            longitude = -122.4194
        )

        assertEquals(37.7749, geoPoint.latitude)
        assertEquals(-122.4194, geoPoint.longitude)
    }

    @Test
    fun `GeoPoint should accept valid latitude range -90 to 90`() {
        val minLat = GeoPoint(latitude = -90.0, longitude = 0.0)
        val maxLat = GeoPoint(latitude = 90.0, longitude = 0.0)

        assertEquals(-90.0, minLat.latitude)
        assertEquals(90.0, maxLat.latitude)
    }

    @Test
    fun `GeoPoint should accept valid longitude range -180 to 180`() {
        val minLon = GeoPoint(latitude = 0.0, longitude = -180.0)
        val maxLon = GeoPoint(latitude = 0.0, longitude = 180.0)

        assertEquals(-180.0, minLon.longitude)
        assertEquals(180.0, maxLon.longitude)
    }

    @Test
    fun `GeoPoint should reject latitude below -90`() {
        assertFailsWith<IllegalArgumentException> {
            GeoPoint(latitude = -90.1, longitude = 0.0)
        }
    }

    @Test
    fun `GeoPoint should reject latitude above 90`() {
        assertFailsWith<IllegalArgumentException> {
            GeoPoint(latitude = 90.1, longitude = 0.0)
        }
    }

    @Test
    fun `GeoPoint should reject longitude below -180`() {
        assertFailsWith<IllegalArgumentException> {
            GeoPoint(latitude = 0.0, longitude = -180.1)
        }
    }

    @Test
    fun `GeoPoint should reject longitude above 180`() {
        assertFailsWith<IllegalArgumentException> {
            GeoPoint(latitude = 0.0, longitude = 180.1)
        }
    }

    @Test
    fun `GeoPoint should support equality`() {
        val point1 = GeoPoint(37.7749, -122.4194)
        val point2 = GeoPoint(37.7749, -122.4194)

        assertEquals(point1, point2)
    }

    // endregion

    // region SetOptions - matches Firebase SetOptions

    @Test
    fun `SetOptions Overwrite should be default behavior`() {
        val options: SetOptions = SetOptions.Overwrite

        assertIs<SetOptions.Overwrite>(options)
    }

    @Test
    fun `SetOptions Merge should merge with existing data`() {
        val options: SetOptions = SetOptions.Merge

        assertIs<SetOptions.Merge>(options)
    }

    @Test
    fun `SetOptions MergeFields should accept list of fields`() {
        val options = SetOptions.MergeFields(listOf("name", "email", "age"))

        assertEquals(listOf("name", "email", "age"), options.fields)
    }

    @Test
    fun `SetOptions MergeFields should accept vararg fields`() {
        val options = SetOptions.MergeFields("name", "email", "age")

        assertEquals(listOf("name", "email", "age"), options.fields)
    }

    @Test
    fun `SetOptions should be a sealed class`() {
        val options: List<SetOptions> = listOf(
            SetOptions.Overwrite,
            SetOptions.Merge,
            SetOptions.MergeFields("field")
        )

        assertEquals(3, options.size)
    }

    // endregion

    // region Source - matches Firebase Source

    @Test
    fun `Source should have DEFAULT SERVER and CACHE values`() {
        assertEquals(3, Source.entries.size)
        assertEquals(Source.DEFAULT, Source.entries[0])
        assertEquals(Source.SERVER, Source.entries[1])
        assertEquals(Source.CACHE, Source.entries[2])
    }

    @Test
    fun `Source DEFAULT should be the default fetch behavior`() {
        val source = Source.DEFAULT

        assertEquals(Source.DEFAULT, source)
    }

    @Test
    fun `Source SERVER should force server fetch`() {
        val source = Source.SERVER

        assertEquals(Source.SERVER, source)
    }

    @Test
    fun `Source CACHE should force cache read`() {
        val source = Source.CACHE

        assertEquals(Source.CACHE, source)
    }

    // endregion
}

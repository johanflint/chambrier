package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.IncrementNumberValue
import com.larastudios.chambrier.app.domain.SetBooleanValue
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("MergeCommandMaps")
class MergeCommandMapsTest {
    private val property = mapOf("on" to SetBooleanValue(true))
    private val propertyTwo = mapOf("brightness" to IncrementNumberValue(1))
    private val propertyThree = mapOf("on" to SetBooleanValue(false))

    @Test
    fun `merges an empty map with a map containing one device and one property`() {
        val mergedMap = mergeCommandMaps(mapOf(), mapOf("42" to property))
        assertThat(mergedMap).containsExactly(entry("42", property))
    }

    @Test
    fun `merges a map with one device with a map containing another device`() {
        val mergedMap = mergeCommandMaps(mapOf("42" to property), mapOf("43" to propertyTwo))
        assertThat(mergedMap).containsExactly(
            entry("42", property),
            entry("43", propertyTwo),
        )
    }

    @Test
    fun `merge adds a new property to an existing device`() {
        val mergedMap = mergeCommandMaps(mapOf("42" to property), mapOf("42" to propertyTwo))
        assertThat(mergedMap).containsExactly(entry("42", mapOf(
            "on" to SetBooleanValue(true),
            "brightness" to IncrementNumberValue(1),
        )))
    }

    @Test
    fun `merge overwrites an existing property for an existing device`() {
        val mergedMap = mergeCommandMaps(mapOf("42" to property), mapOf("42" to propertyTwo + propertyThree))
        assertThat(mergedMap).containsExactly(entry("42", mapOf(
            "on" to SetBooleanValue(false),
            "brightness" to IncrementNumberValue(1),
        )))
    }
}

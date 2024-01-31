package com.larastudios.chambrier.adapter.hue

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("mirekToKelvin")
class KelvinConversionsTest {
    @ParameterizedTest(name = "{0} mirek = {1}K")
    @CsvSource(
        "153, 6535",
        "500, 2000"
    )
    fun `returns the value in Kelvin`(mirek: Int, kelvin: Int) {
        Assertions.assertThat(mirekToKelvin(mirek)).isEqualTo(kelvin)
    }

    @ParameterizedTest(name = "{0} kelvin = {1} mirek")
    @CsvSource(
        "6535, 153",
        "2000, 500"
    )
    fun `returns the value in mirek`(kelvin: Int, mirek: Int) {
        Assertions.assertThat(kelvinToMirek(kelvin)).isEqualTo(mirek)
    }
}

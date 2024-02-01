package com.larastudios.chambrier.app.domain

import com.larastudios.chambrier.booleanProperty
import com.larastudios.chambrier.colorProperty
import com.larastudios.chambrier.enumProperty
import com.larastudios.chambrier.numberProperty
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

@DisplayName("PropertyValue")
class PropertyValuesTest {

    @ParameterizedTest(name = "{0} isAssignableTo({1}) = {2}")
    @MethodSource("propertyValueProvider")
    fun isAssignableTo(propertyValue: PropertyValue, property: Property, expected: Boolean) {
        val actual = propertyValue.isAssignableTo(property)
        assertThat(expected).isEqualTo(actual)
    }

    companion object {
        @JvmStatic
        fun propertyValueProvider(): List<Arguments> = listOf(
            Arguments.of(SetBooleanValue(true).toNamed(), booleanProperty.toNamed(), true),
            Arguments.of(ToggleBooleanValue.toNamed(), booleanProperty.toNamed(), true),
            Arguments.of(SetNumberValue(42).toNamed(), numberProperty.toNamed(), true),
            Arguments.of(IncrementNumberValue(5).toNamed(), numberProperty.toNamed(), true),
            Arguments.of(DecrementNumberValue(3).toNamed(), numberProperty.toNamed(), true),
            Arguments.of(SetColorValue(CartesianCoordinate(1.0, 1.0)).toNamed(), colorProperty.toNamed(), true),
            Arguments.of(SetEnumValue(HueButtonState.LongPress), enumProperty.toNamed(), true),
        )

        private inline fun <reified T> T.toNamed(): Named<T> = named(T::class.simpleName, this)
    }
}

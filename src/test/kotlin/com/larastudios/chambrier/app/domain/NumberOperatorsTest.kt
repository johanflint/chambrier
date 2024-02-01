package com.larastudios.chambrier.app.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

@DisplayName("NumberOperators")
class NumberOperatorsTest {
    @ParameterizedTest(name = "{0} + {1} = {2} (type: {3})")
    @MethodSource("plusNumbersProvider")
    fun `+ operator`(one: Number, two: Number, expected: Number, type: KClass<Any>) {
        val actual = one + two

        assertThat(actual).isEqualTo(expected)
        assertThat(actual::class).isEqualTo(type)
    }

    @ParameterizedTest(name = "{0} - {1} = {2} (type: {3})")
    @MethodSource("minusNumbersProvider")
    fun `- operator`(one: Number, two: Number, expected: Number, type: KClass<Any>) {
        val actual = one - two

        assertThat(actual).isEqualTo(expected)
        assertThat(actual::class).isEqualTo(type)
    }

    @ParameterizedTest(name = "{0} / {1} = {2} (type: {3})")
    @MethodSource("divNumbersProvider")
    fun `div operator`(one: Number, two: Number, expected: Number, type: KClass<Any>) {
        val actual = one / two

        assertThat(actual).isEqualTo(expected)
        assertThat(actual::class).isEqualTo(type)
    }

    @ParameterizedTest(name = "{0}.coerceInNullable({1}, {2}) = {3}")
    @MethodSource("coerceNumbersProvider")
    fun coerceInNullable(value: Number, minimum: Number, maximum: Number, expected: Number) {
        val actual = value.coerceInNullable(minimum, maximum)

        assertThat(actual).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        fun plusNumbersProvider(): List<Arguments> = listOf(
            // Int
            Arguments.of(2, 3, 5, Int::class),
            Arguments.of(2, 3L, 5L, Long::class),
            Arguments.of(2, 3.1, 5.1, Double::class),

            // Long
            Arguments.of(2L, 3, 5L, Long::class),
            Arguments.of(2L, 3L, 5L, Long::class),
            Arguments.of(2L, 3.1, 5.1, Double::class),

            // Double
            Arguments.of(2.0, 3, 5.0, Double::class),
            Arguments.of(2.0, 3L, 5.0, Double::class),
            Arguments.of(2.0, 3.1, 5.1, Double::class),
        )

        @JvmStatic
        fun minusNumbersProvider(): List<Arguments> = listOf(
            /// Int
            Arguments.of(4, 3, 1, Int::class),
            Arguments.of(4, 3L, 1L, Long::class),
            Arguments.of(4, 3.0, 1.0, Double::class),

            // Long
            Arguments.of(4L, 3, 1L, Long::class),
            Arguments.of(4L, 3L, 1L, Long::class),
            Arguments.of(4L, 3.0, 1.0, Double::class),

            // Double
            Arguments.of(4.0, 3, 1.0, Double::class),
            Arguments.of(4.0, 3L, 1.0, Double::class),
            Arguments.of(4.0, 3.0, 1.0, Double::class),
        )

        @JvmStatic
        fun divNumbersProvider(): List<Arguments> = listOf(
            /// Int
            Arguments.of(9, 3, 3, Int::class),
            Arguments.of(9, 3L, 3L, Long::class),
            Arguments.of(9, 3.0, 3.0, Double::class),

            // Long
            Arguments.of(9L, 3, 3L, Long::class),
            Arguments.of(9L, 3L, 3L, Long::class),
            Arguments.of(9L, 3.0, 3.0, Double::class),

            // Double
            Arguments.of(9.0, 3, 3.0, Double::class),
            Arguments.of(9.0, 3L, 3.0, Double::class),
            Arguments.of(9.0, 3.0, 3.0, Double::class),
        )

        @JvmStatic
        fun coerceNumbersProvider(): List<Arguments> = listOf(
            // Int
            Arguments.of(42, 0, 100, 42),
            Arguments.of(42, 50, 100, 50),
            Arguments.of(42, 0, 41, 41),

            // Long
            Arguments.of(42L, 0L, 100L, 42L),
            Arguments.of(42L, 50L, 100L, 50L),
            Arguments.of(42L, 0L, 41L, 41L),

            // Double
            Arguments.of(42.0, 0.0, 100.0, 42.0),
            Arguments.of(42.0, 50.0, 100.0, 50.0),
            Arguments.of(42.0, 0.0, 41.0, 41.0),
        )
    }
}

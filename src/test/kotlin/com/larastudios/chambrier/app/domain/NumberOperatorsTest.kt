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

    companion object {
        @JvmStatic
        fun plusNumbersProvider(): List<Arguments> = listOf(
            Arguments.of(2, 3, 5, Int::class),
            Arguments.of(2, 3L, 5L, Long::class),
            Arguments.of(2, 3.1, 5.1, Double::class),

            Arguments.of(2L, 3, 5L, Long::class),
            Arguments.of(2L, 3L, 5L, Long::class),
            Arguments.of(2L, 3.1, 5.1, Double::class),

            Arguments.of(2.0, 3, 5.0, Double::class),
            Arguments.of(2.0, 3L, 5.0, Double::class),
            Arguments.of(2.0, 3.1, 5.1, Double::class),
        )

        @JvmStatic
        fun minusNumbersProvider(): List<Arguments> = listOf(
            Arguments.of(4, 3, 1, Int::class),
            Arguments.of(4, 3L, 1L, Long::class),
            Arguments.of(4, 3.0, 1.0, Double::class),

            Arguments.of(4L, 3, 1L, Long::class),
            Arguments.of(4L, 3L, 1L, Long::class),
            Arguments.of(4L, 3.0, 1.0, Double::class),

            Arguments.of(4.0, 3, 1.0, Double::class),
            Arguments.of(4.0, 3L, 1.0, Double::class),
            Arguments.of(4.0, 3.0, 1.0, Double::class),
        )
    }
}

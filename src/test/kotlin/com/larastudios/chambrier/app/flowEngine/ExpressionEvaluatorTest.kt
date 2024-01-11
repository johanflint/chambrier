package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.flowEngine.expression.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("evaluateExpression")
class ExpressionEvaluatorTest {
    @Nested
    @DisplayName("evaluating greater than or equal to")
    inner class GreaterThanOrEqualToTests {
        @ParameterizedTest(name = "{0} >= {1} is {2}")
        @CsvSource(
            "4, 8, false",
            "4, 4, true",
            "8, 4, true")
        fun `returns a boolean `(left: Int, right: Int, expected: Boolean) {
            val expression = GreaterThanOrEqualToExpression(ConstantValueExpression(left), ConstantValueExpression(right))
            val actual = evaluateExpression(expression)

            assertThat(actual).isEqualTo(expected)
        }

        @Test
        fun `throws if different types are used`() {
            val expression = GreaterThanOrEqualToExpression(ConstantValueExpression(2), ConstantValueExpression(2.0))
            Assertions.assertThatExceptionOfType(ExpressionEvaluationException::class.java)
                .isThrownBy {
                    evaluateExpression(expression)
                }
                .withMessage("Cannot compare two values of different types: left '2' of type class kotlin.Int, right '2.0' of type class kotlin.Double")
        }
    }

    @Nested
    @DisplayName("evaluating greater than")
    inner class GreaterThanTests {
        @ParameterizedTest(name = "{0} > {1} is {2}")
        @CsvSource(
            "4, 8, false",
            "4, 4, false",
            "8, 4, true")
        fun `returns a boolean `(left: Int, right: Int, expected: Boolean) {
            val expression = GreaterThanExpression(ConstantValueExpression(left), ConstantValueExpression(right))
            val actual = evaluateExpression(expression)

            assertThat(actual).isEqualTo(expected)
        }

        @Test
        fun `throws if different types are used`() {
            val expression = GreaterThanExpression(ConstantValueExpression(2), ConstantValueExpression(2.0))
            Assertions.assertThatExceptionOfType(ExpressionEvaluationException::class.java)
                .isThrownBy {
                    evaluateExpression(expression)
                }
                .withMessage("Cannot compare two values of different types: left '2' of type class kotlin.Int, right '2.0' of type class kotlin.Double")
        }
    }

    @Nested
    @DisplayName("evaluating less than")
    inner class LessThanTests {
        @ParameterizedTest(name = "{0} < {1} is {2}")
        @CsvSource(
            "4, 8, true",
            "4, 4, false",
            "8, 4, false")
        fun `returns a boolean `(left: Int, right: Int, expected: Boolean) {
            val expression = LessThanExpression(ConstantValueExpression(left), ConstantValueExpression(right))
            val actual = evaluateExpression(expression)

            assertThat(actual).isEqualTo(expected)
        }

        @Test
        fun `throws if different types are used`() {
            val expression = LessThanExpression(ConstantValueExpression(2), ConstantValueExpression(2.0))
            Assertions.assertThatExceptionOfType(ExpressionEvaluationException::class.java)
                .isThrownBy {
                    evaluateExpression(expression)
                }
                .withMessage("Cannot compare two values of different types: left '2' of type class kotlin.Int, right '2.0' of type class kotlin.Double")
        }
    }

    @Nested
    @DisplayName("evaluating less than or equal to")
    inner class LessThanOrEqualToTests {
        @ParameterizedTest(name = "{0} <= {1} is {2}")
        @CsvSource(
            "4, 8, true",
            "4, 4, true",
            "8, 4, false")
        fun `returns a boolean `(left: Int, right: Int, expected: Boolean) {
            val expression = LessThanOrEqualToExpression(ConstantValueExpression(left), ConstantValueExpression(right))
            val actual = evaluateExpression(expression)

            assertThat(actual).isEqualTo(expected)
        }

        @Test
        fun `throws if different types are used`() {
            val expression = LessThanOrEqualToExpression(ConstantValueExpression(2), ConstantValueExpression(2.0))
            Assertions.assertThatExceptionOfType(ExpressionEvaluationException::class.java)
                .isThrownBy {
                    evaluateExpression(expression)
                }
                .withMessage("Cannot compare two values of different types: left '2' of type class kotlin.Int, right '2.0' of type class kotlin.Double")
        }
    }

    @Nested
    @DisplayName("evaluating equal to")
    inner class EqualToTests {
        @ParameterizedTest(name = "{0} == {1} is {2}")
        @CsvSource(
            "4, 4, true",
            "4, 8, false",
            "4.0, 4.0, true",
            "4.0, 4.08, false",
            "4L, 4L, true",
            "4L, 8L, false",
            "name, name, true",
            "name, Name, false",
            "false, false, true",
            "false, true, false"
        )
        fun `returns a boolean `(left: Any, right: Any, expected: Boolean) {
            val expression = EqualToExpression(ConstantValueExpression(left), ConstantValueExpression(right))
            val actual = evaluateExpression(expression)

            assertThat(actual).isEqualTo(expected)
        }
    }

    @Nested
    @DisplayName("evaluating not equal to")
    inner class NotEqualToTests {
        @ParameterizedTest(name = "{0} != {1} is {2}")
        @CsvSource(
            "4, 4, false",
            "4, 8, true",
            "4.0, 4.0, false",
            "4.0, 4.08, true",
            "4L, 4L, false",
            "4L, 8L, true",
            "name, name, false",
            "name, Name, true",
            "true, true, false",
            "true, false, true"
        )
        fun `returns a boolean `(left: Any, right: Any, expected: Boolean) {
            val expression = NotEqualToExpression(ConstantValueExpression(left), ConstantValueExpression(right))
            val actual = evaluateExpression(expression)

            assertThat(actual).isEqualTo(expected)
        }
    }

    @Nested
    @DisplayName("evaluating and")
    inner class AndTests {
        @ParameterizedTest(name = "{0} && {1} is {2}")
        @CsvSource(
            "true, true, true",
            "true, false, false",
            "false, false, false",
            "false, true, false",

        )
        fun `returns a boolean `(left: Boolean, right: Boolean, expected: Boolean) {
            val expression = AndExpression(ConstantValueExpression(left), ConstantValueExpression(right))
            val actual = evaluateExpression(expression)

            assertThat(actual).isEqualTo(expected)
        }
    }

    @Nested
    @DisplayName("evaluating or")
    inner class OrTests {
        @ParameterizedTest(name = "{0} || {1} is {2}")
        @CsvSource(
            "true, true, true",
            "true, false, true",
            "false, false, false",
            "false, true, true",

            )
        fun `returns a boolean `(left: Boolean, right: Boolean, expected: Boolean) {
            val expression = OrExpression(ConstantValueExpression(left), ConstantValueExpression(right))
            val actual = evaluateExpression(expression)

            assertThat(actual).isEqualTo(expected)
        }
    }

    @Nested
    @DisplayName("evaluating a constant value")
    inner class ConstantValueTests {
        @Test
        fun `returns the value `() {
            assertThat(evaluateExpression(ConstantValueExpression(42))).isEqualTo(42)
            assertThat(evaluateExpression(ConstantValueExpression(4.8))).isEqualTo(4.8)
            assertThat(evaluateExpression(ConstantValueExpression(4.8f))).isEqualTo(4.8f)
            assertThat(evaluateExpression(ConstantValueExpression(1337L))).isEqualTo(1337L)
            assertThat(evaluateExpression(ConstantValueExpression(true))).isEqualTo(true)
            assertThat(evaluateExpression(ConstantValueExpression("string"))).isEqualTo("string")
        }
    }
}

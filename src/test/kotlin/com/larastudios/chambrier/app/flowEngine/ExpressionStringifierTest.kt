package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.flowEngine.expression.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Expression.stringify")
class ExpressionStringifierTest {
    @Test
    fun `stringifies greater than or equal to`() {
        val expression = GreaterThanOrEqualToExpression(ConstantValueExpression(4), ConstantValueExpression(8))
        assertThat(expression.stringify()).isEqualTo("4 >= 8")
    }

    @Test
    fun `stringifies greater than`() {
        val expression = GreaterThanExpression(ConstantValueExpression(4), ConstantValueExpression(8))
        assertThat(expression.stringify()).isEqualTo("4 > 8")
    }

    @Test
    fun `stringifies less than`() {
        val expression = LessThanExpression(ConstantValueExpression(4), ConstantValueExpression(8))
        assertThat(expression.stringify()).isEqualTo("4 < 8")
    }

    @Test
    fun `stringifies less than or equal to`() {
        val expression = LessThanOrEqualToExpression(ConstantValueExpression(4), ConstantValueExpression(8))
        assertThat(expression.stringify()).isEqualTo("4 <= 8")
    }

    @Test
    fun `stringifies equal to`() {
        val expression = EqualToExpression(ConstantValueExpression(4), ConstantValueExpression(8))
        assertThat(expression.stringify()).isEqualTo("4 == 8")
    }

    @Test
    fun `stringifies not equal to`() {
        val expression = NotEqualToExpression(ConstantValueExpression(4), ConstantValueExpression(8))
        assertThat(expression.stringify()).isEqualTo("4 != 8")
    }

    @Test
    fun `stringifies and`() {
        val expression = AndExpression(ConstantValueExpression(4), ConstantValueExpression(8))
        assertThat(expression.stringify()).isEqualTo("4 && 8")
    }

    @Test
    fun `stringifies or`() {
        val expression = OrExpression(ConstantValueExpression(4), ConstantValueExpression(8))
        assertThat(expression.stringify()).isEqualTo("4 || 8")
    }

    @Test
    fun `stringifies a constant value`() {
        assertThat(ConstantValueExpression(42).stringify()).isEqualTo("42")
        assertThat(ConstantValueExpression(4.8).stringify()).isEqualTo("4.8")
        assertThat(ConstantValueExpression(4.8f).stringify()).isEqualTo("4.8")
        assertThat(ConstantValueExpression(1337L).stringify()).isEqualTo("1337")
        assertThat(ConstantValueExpression(true).stringify()).isEqualTo("true")
        assertThat(ConstantValueExpression("string").stringify()).isEqualTo("string")
    }
}

package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.flowEngine.expression.*

fun evaluateExpression(expression: Expression): Any {
    return when (expression) {
        // Comparison
        is GreaterThanOrEqualToExpression -> {
            val left = evaluateExpression(expression.left)
            val right = evaluateExpression(expression.right)

            return compare<Comparable<Any>>(left, right) { l, r -> l >= r }
        }
        is GreaterThanExpression -> {
            val left = evaluateExpression(expression.left)
            val right = evaluateExpression(expression.right)

            return compare<Comparable<Any>>(left, right) { l, r -> l > r }
        }
        is LessThanExpression -> {
            val left = evaluateExpression(expression.left)
            val right = evaluateExpression(expression.right)

            return compare<Comparable<Any>>(left, right) { l, r -> l < r }
        }
        is LessThanOrEqualToExpression -> {
            val left = evaluateExpression(expression.left)
            val right = evaluateExpression(expression.right)

            return compare<Comparable<Any>>(left, right) { l, r -> l <= r }
        }

        // Equality
        is EqualToExpression -> {
            val left = evaluateExpression(expression.left)
            val right = evaluateExpression(expression.right)
            left == right
        }
        is NotEqualToExpression -> {
            val left = evaluateExpression(expression.left)
            val right = evaluateExpression(expression.right)
            left != right
        }

        // Logic
        is AndExpression -> {
            val left = evaluateExpression(expression.left)
            val right = evaluateExpression(expression.right)
            if (left is Boolean && right is Boolean) {
                left && right
            } else throw ExpressionEvaluationException("Only boolean values can be used with the 'and' operator: $left && $right")
        }
        is OrExpression -> {
            val left = evaluateExpression(expression.left)
            val right = evaluateExpression(expression.right)
            if (left is Boolean && right is Boolean) {
                left || right
            } else throw ExpressionEvaluationException("Only boolean values can be used with the 'or' operator: $left || $right")
        }

        // Value
        is ConstantValueExpression<*> -> expression.value
        else -> throw ExpressionEvaluationException("Unknown expression type: ${expression::class}")
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : Comparable<T>> compare(left: Any, right: Any, operation: (T, T) -> Boolean): Boolean {
    if (left::class == right::class) {
        val castLeft = left as? T
        val castRight = right as? T

        return castLeft != null && castRight != null && operation(castLeft, castRight)
    }

    throw ExpressionEvaluationException("Cannot compare two values of different types: left '$left' of type ${left::class}, right '$right' of type ${right::class}")
}

class ExpressionEvaluationException(override val message: String?) : Exception(message)

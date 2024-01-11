package com.larastudios.chambrier.app.flowEngine

import com.larastudios.chambrier.app.flowEngine.expression.*

fun Expression.stringify(): String {
    return when (this) {
        // Comparison
        is GreaterThanOrEqualToExpression -> "${left.stringify()} >= ${right.stringify()}"
        is GreaterThanExpression -> "${left.stringify()} > ${right.stringify()}"
        is LessThanExpression -> "${left.stringify()} < ${right.stringify()}"
        is LessThanOrEqualToExpression -> "${left.stringify()} <= ${right.stringify()}"

        // Equality
        is EqualToExpression -> "${left.stringify()} == ${right.stringify()}"
        is NotEqualToExpression -> "${left.stringify()} != ${right.stringify()}"

        // Logic
        is AndExpression -> "${left.stringify()} && ${right.stringify()}"
        is OrExpression -> "${left.stringify()} || ${right.stringify()}"

        // Value
        is ConstantValueExpression<*> -> "$value"
        else -> throw UnknownExpressionTypeException(this.toString())
    }
}

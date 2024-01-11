package com.larastudios.chambrier.app.flowEngine.expression

data class GreaterThanOrEqualToExpression(val left: Expression, val right: Expression): Expression

data class GreaterThanExpression(val left: Expression, val right: Expression): Expression

data class LessThanExpression(val left: Expression, val right: Expression): Expression

data class LessThanOrEqualToExpression(val left: Expression, val right: Expression): Expression

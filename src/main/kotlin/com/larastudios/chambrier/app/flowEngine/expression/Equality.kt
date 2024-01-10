package com.larastudios.chambrier.app.flowEngine.expression

data class EqualToExpression(val left: Expression, val right: Expression): Expression

data class NotEqualToExpression(val left: Expression, val right: Expression): Expression

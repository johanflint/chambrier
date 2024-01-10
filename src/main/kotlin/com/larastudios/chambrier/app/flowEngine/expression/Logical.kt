package com.larastudios.chambrier.app.flowEngine.expression

data class AndExpression(val left: Expression, val right: Expression): Expression

data class OrExpression(val left: Expression, val right: Expression): Expression

package com.larastudios.chambrier.app.flowEngine.expression

interface ValueExpression : Expression

data class ConstantValueExpression<T : Any>(val value: T) : ValueExpression

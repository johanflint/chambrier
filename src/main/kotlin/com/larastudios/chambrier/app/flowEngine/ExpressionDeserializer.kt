package com.larastudios.chambrier.app.flowEngine

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.larastudios.chambrier.app.flowEngine.expression.*
import org.springframework.stereotype.Component

/**
 * Deserializes [Expression] from JSON.
 *
 * The reason expressions do not have SerializedExpression variants is to make it reduce the amount of types needed
 * and to avoid copying over a tree of SerializedExpressions to Expressions. This is acceptable because these are small
 * data classes that are unlikely to change.
 */
@Component
class ExpressionDeserializer : StdDeserializer<Expression>(Expression::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Expression {
        val node: JsonNode = p.codec.readTree(p)
        return when (val type = node.get("type").asText()) {
            // Comparison
            "greaterThanOrEqualTo" -> p.codec.treeToValue(node, GreaterThanOrEqualToExpression::class.java)
            "greaterThan" -> p.codec.treeToValue(node, GreaterThanExpression::class.java)
            "lessThan" -> p.codec.treeToValue(node, LessThanExpression::class.java)
            "lessThanOrEqualTo" -> p.codec.treeToValue(node, LessThanOrEqualToExpression::class.java)

            // Equality
            "equalTo" -> p.codec.treeToValue(node, EqualToExpression::class.java)
            "notEqualTo" -> p.codec.treeToValue(node, NotEqualToExpression::class.java)

            // Logic
            "and" -> p.codec.treeToValue(node, AndExpression::class.java)
            "or" -> p.codec.treeToValue(node, OrExpression::class.java)

            // Value
            "constant" -> p.codec.treeToValue(node, ConstantValueExpression::class.java)
            else -> throw UnknownExpressionTypeException(type)
        }
    }
}

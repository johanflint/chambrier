package com.larastudios.chambrier.app.flowEngine

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.larastudios.chambrier.app.domain.*
import org.springframework.stereotype.Component

@Component
class PropertyValueDeserializer : StdDeserializer<PropertyValue>(PropertyValue::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PropertyValue {
        val node: JsonNode = p.codec.readTree(p)
        return when (val type = node.get("type").asText()) {
            // Boolean
            "boolean" -> p.codec.treeToValue(node, SetBooleanValue::class.java)
            "toggle" -> ToggleBooleanValue

            // Number
            "number" -> p.codec.treeToValue(node, SetNumberValue::class.java)
            "increment" -> p.codec.treeToValue(node, IncrementNumberValue::class.java)
            "decrement" -> p.codec.treeToValue(node, DecrementNumberValue::class.java)

            // Color
            "color" -> p.codec.treeToValue(node, SetColorValue::class.java)

            // Enum
            "enum" -> {
                val (enumType, value) = node.get("value").asText().split(".")
                return when (enumType) {
                    HueButtonState::class.simpleName -> SetEnumValue(enumValueOf<HueButtonState>(value))
                    else -> throw UnknownEnumPropertyValueException("Unknown enum type '$enumType'")
                }
            }

            else -> throw UnknownPropertyValue(type, node.toPrettyString())
        }
    }
}

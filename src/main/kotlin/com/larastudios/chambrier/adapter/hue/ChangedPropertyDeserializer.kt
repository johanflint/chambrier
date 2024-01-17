package com.larastudios.chambrier.adapter.hue

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.springframework.stereotype.Component

@Component
class ChangedPropertyDeserializer : StdDeserializer<ChangedProperty>(ChangedProperty::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ChangedProperty {
        val node: JsonNode = p.codec.readTree(p)
        return when (val type = node.get("type").asText()) {
            "light" -> p.codec.treeToValue(node, ChangedLightProperty::class.java)
            else -> UnknownChangedProperty(
                id = node.get("id").asText(),
                type = type,
                payload = node.toString()
            )
        }
    }
}

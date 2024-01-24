package com.larastudios.chambrier.adapter

import com.fasterxml.jackson.databind.module.SimpleModule
import com.larastudios.chambrier.adapter.hue.sse.ChangedProperty
import com.larastudios.chambrier.adapter.hue.sse.ChangedPropertyDeserializer
import com.larastudios.chambrier.app.domain.PropertyValue
import com.larastudios.chambrier.app.flowEngine.ExpressionDeserializer
import com.larastudios.chambrier.app.flowEngine.PropertyValueDeserializer
import com.larastudios.chambrier.app.flowEngine.expression.Expression
import org.springframework.stereotype.Component

@Component
class CustomDeserializers {
    fun module(): SimpleModule =
        SimpleModule()
            .addDeserializer(ChangedProperty::class.java, ChangedPropertyDeserializer())
            .addDeserializer(Expression::class.java, ExpressionDeserializer())
            .addDeserializer(PropertyValue::class.java, PropertyValueDeserializer())
}

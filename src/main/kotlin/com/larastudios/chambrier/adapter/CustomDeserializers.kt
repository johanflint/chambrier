package com.larastudios.chambrier.adapter

import com.fasterxml.jackson.databind.module.SimpleModule
import com.larastudios.chambrier.adapter.hue.ChangedProperty
import com.larastudios.chambrier.adapter.hue.ChangedPropertyDeserializer
import org.springframework.stereotype.Component

@Component
class CustomDeserializers {
    fun module(): SimpleModule =
        SimpleModule().addDeserializer(ChangedProperty::class.java, ChangedPropertyDeserializer())
}

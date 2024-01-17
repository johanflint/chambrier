package com.larastudios.chambrier

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.larastudios.chambrier.adapter.CustomDeserializers
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class ObjectMapperConfiguration {
    @Primary
    @Bean
    fun objectMapper(customDeserializers: CustomDeserializers): ObjectMapper = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .build()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .registerModule(customDeserializers.module())

    @Bean
    @SnakeCased
    fun snakeCasedObjectMapper(customDeserializers: CustomDeserializers): ObjectMapper =
        objectMapper(customDeserializers).copy().setPropertyNamingStrategy(PropertyNamingStrategies.SnakeCaseStrategy())
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
annotation class SnakeCased

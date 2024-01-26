package com.larastudios.chambrier.app

import com.larastudios.chambrier.app.domain.PropertyValue

typealias CommandMap = Map<String, PropertyMap>
typealias PropertyMap = Map<String, PropertyValue>

fun mergeCommandMaps(mergedMap: CommandMap, otherMap: CommandMap): CommandMap =
    otherMap.entries.fold(mergedMap) { commandMap, (deviceId, propertyMap) ->
        val map = if (commandMap.containsKey(deviceId)) {
            mapOf(deviceId to commandMap[deviceId]!!.plus(propertyMap))
        } else {
            mapOf(deviceId to propertyMap)
        }

        commandMap + map
    }

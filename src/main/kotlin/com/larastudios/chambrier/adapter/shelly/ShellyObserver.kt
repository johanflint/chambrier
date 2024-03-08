package com.larastudios.chambrier.adapter.shelly

import com.fasterxml.jackson.databind.ObjectMapper
import com.larastudios.chambrier.app.Observer
import com.larastudios.chambrier.app.domain.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import java.net.Inet4Address
import java.net.InetAddress
import javax.jmdns.JmDNS

@RestController
@ConditionalOnProperty("shelly.enabled")
class ShellyObserver(
    val objectMapper: ObjectMapper,
    val webClientBuilder: WebClient.Builder,
    @Value("\${server.port}") val port: Int,
) : Observer {
    private val flow = MutableSharedFlow<Event>(replay = 1)

    @GetMapping("/hooks/shelly-one/{id}")
    suspend fun hook(@PathVariable id: String, @RequestParam switch: Boolean) {
        logger.debug { "Received webhook call for device '$id' with value '$switch'" }
        val propertyChanged = BooleanPropertyChanged(deviceId = id, propertyId = "on", value = switch)
        flow.tryEmit(propertyChanged)
    }

    override suspend fun observe(): Flow<Event> = coroutineScope {
        val localhost = withContext(Dispatchers.IO) {
            InetAddress.getLocalHost()
        }

        launch {
            val addresses = discover(localhost).await()
            val devices = addresses.map { configure(it, localhost) }.awaitAll()
            flow.tryEmit(DiscoveredDevices(devices))
        }

        flow
    }

    private suspend fun discover(localhost: InetAddress): Deferred<List<Inet4Address>> = coroutineScope {
        async {
            withContext(Dispatchers.IO) {
                logger.info { "[mDNS] Sending query..." }
                val dns = JmDNS.create(localhost)
                dns.list("_http._tcp.local.")
                    .filter {it.inetAddresses.isNotEmpty() && it.name.contains("shelly") }
                    .mapNotNull { it.inet4Addresses.firstOrNull() }
                    .also { logger.info { "[mDNS] Sending query... OK, ${it.size} response(s)" } }
            }
        }
    }

    private suspend fun configure(deviceAddress: InetAddress, localhost: InetAddress) = coroutineScope {
        val webClient = webClientBuilder.baseUrl(deviceAddress.hostAddress)
            .codecs { it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper)) }
            .build()
        val client = ShellyClient(webClient)

        async {
            val deviceInfo = client.deviceInfo()
            if (deviceInfo.model != "SNSW-001X16EU") { // Shelly Plus 1
                throw UnsupportedDeviceException("Unknown Shelly model: ${deviceInfo.model}")
            }

            val webhooks = client.listHooks()
            val onHookFound = webhooks.any { it.enable && it.event == SWITCH_ON_EVENT }
            if (!onHookFound) {
                val body = createHookRequest(true, localhost, port)
                client.createHook(body)
                logger.info { "Created webhook for device '${deviceInfo.id}' for event '$SWITCH_ON_EVENT'" }
            }

            val offHookFound = webhooks.any { it.enable && it.event == SWITCH_OFF_EVENT }
            if (!offHookFound) {
                val body = createHookRequest(false, localhost, 8080)
                client.createHook(body)
                logger.info { "Created webhook for device '${deviceInfo.id}' for event '$SWITCH_OFF_EVENT'" }
            }

            val status = client.status()
            Device(
                deviceInfo.id,
                DeviceType.Switch,
                "Shelly",
                deviceInfo.model,
                "Plus 1",
                deviceInfo.name,
                properties = mapOf(
                    "on" to BooleanProperty(
                        name = "on",
                        type = PropertyType.On,
                        readonly = false,
                        value = status.switch0.output
                    )
                ),
                externalId = deviceAddress.hostAddress
            )
        }
    }

    suspend fun createHookRequest(switch: Boolean, localhost: InetAddress, port: Int): WebhookCreateRequestBody {
        val url = "http://${localhost.hostAddress}:$port/hooks/shelly-one/\${info.id}?switch=$switch"
        val event = if (switch) SWITCH_ON_EVENT else SWITCH_OFF_EVENT
        val name = if (switch) "on" else "off"
        return WebhookCreateRequestBody(cid = 0, event, true, "chambrier.switch-$name", listOf(url))
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        const val SWITCH_ON_EVENT = "switch.on"
        const val SWITCH_OFF_EVENT = "switch.off"
    }
}

data class UnsupportedDeviceException(override val message: String) : Exception()

package com.larastudios.chambrier.adapter.shelly

import com.fasterxml.jackson.databind.ObjectMapper
import com.larastudios.chambrier.app.Observer
import com.larastudios.chambrier.app.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.net.Inet4Address
import java.net.InetAddress
import javax.jmdns.JmDNS

@Service
class ShellyObserver(
    val objectMapper: ObjectMapper,
    val webClientBuilder: WebClient.Builder,
) : Observer {
    override suspend fun observe(): Flow<Event> {
        val localhost = withContext(Dispatchers.IO) {
            InetAddress.getLocalHost()
        }

        val addresses = discover(localhost).await()
        val devices = addresses.map { configure(it) }.awaitAll()
        return flowOf<Event>(DiscoveredDevices(devices))
    }

    private suspend fun discover(localhost: InetAddress): Deferred<List<Inet4Address>> = coroutineScope {
        async {
            withContext(Dispatchers.IO) {
                val dns = JmDNS.create(localhost)
                dns.list("_http._tcp.local.")
                    .filter {it.inetAddresses.isNotEmpty() && it.name.contains("shelly") }
                    .mapNotNull { it.inet4Addresses.firstOrNull() }
            }
        }
    }

    private suspend fun configure(deviceAddress: InetAddress) = coroutineScope {
        val webClient = webClientBuilder.baseUrl(deviceAddress.hostAddress)
            .codecs { it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(objectMapper)) }
            .build()
        val client = ShellyClient(webClient)

        async {
            val deviceInfo = client.deviceInfo()
            if (deviceInfo.model != "SNSW-001X16EU") { // Shelly Plus 1
                throw UnsupportedDeviceException("Unknown Shelly model: ${deviceInfo.model}")
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
}

data class UnsupportedDeviceException(override val message: String) : Exception()

package com.larastudios.chambrier.adapter

import com.larastudios.chambrier.app.FlowLoadException
import com.larastudios.chambrier.app.FlowLoader
import com.larastudios.chambrier.app.flowEngine.Flow
import com.larastudios.chambrier.app.flowEngine.FlowFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.name

@Component
class FileFlowLoader(
    @Value("\${flows.directory}")
    private val directory: String,
    private val factory: FlowFactory
) : FlowLoader {
    override suspend fun load(): List<Flow> {
        logger.info { "Loading flows..." }
        val path = Paths.get(directory)
        if (!Files.isDirectory(path)) {
            throw FlowLoadException("Flows directory '$directory' is not a directory")
        }

        val files = withContext(Dispatchers.IO) {
            Files.newDirectoryStream(path, "*.json")
                .use { it.toList() }
                .mapNotNull {
                    logger.debug { "Loading flow ${it.fileName.name}" }
                    val json = Files.readString(it, Charsets.UTF_8)
                    try {
                        factory.fromJson(json)
                    } catch (e: Exception) {
                        logger.warn(e) { "Unable to load flow '${it}'" }
                        null
                    }
                }
        }

        logger.info { "Loading flows... OK, ${files.size} flow(s) loaded" }
        return files
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

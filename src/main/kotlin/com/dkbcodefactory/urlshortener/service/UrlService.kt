package com.dkbcodefactory.urlshortener.service

import com.dkbcodefactory.urlshortener.dtos.UrlGet
import com.dkbcodefactory.urlshortener.dtos.UrlCreate
import com.dkbcodefactory.urlshortener.exceptions.UrlNotFoundException
import io.seruco.encoding.base62.Base62
import org.springframework.stereotype.Service
import java.util.UUID
import org.slf4j.LoggerFactory
import kotlin.jvm.java
import kotlin.text.take

@Service
class UrlService(
    private val persistenceService: PersistenceService
) {
    private val logger = LoggerFactory.getLogger(UrlService::class.java)


    fun createShortUrl(urlCreate: UrlCreate): UrlGet {
        val existing = persistenceService.findUrlByOriginal(urlCreate.originalUrl)
        if (existing != null) {
            logger.info("Existing URL returned: {}", urlCreate.originalUrl)
            return existing
        }

        val id = UUID.randomUUID()
        val shortCode = generateShortCode(id)

        val urlGet = UrlGet(
            id = id,
            originalUrl = urlCreate.originalUrl,
            shorterUrl = shortCode,
            description = urlCreate.description
        )

        persistenceService.saveAndPublish(urlGet)

        return urlGet
    }

    private fun generateShortCode(uuid: UUID): String {
        val base62 = Base62.createInstance()
        val bytes = ByteArray(16)
        val msb = uuid.mostSignificantBits
        val lsb = uuid.leastSignificantBits

        for (i in 0..7) {
            bytes[i] = (msb shr (8 * (7 - i))).toByte()
            bytes[i + 8] = (lsb shr (8 * (7 - i))).toByte()
        }

        val encoded = base62.encode(bytes)
        return kotlin.text.String(encoded).take(8)
    }

    fun resolveShortCode(shortCode: String): String {
        logger.info("Finding URL by short URL: {}", shortCode)
        val url = persistenceService.findUrlByShortUrl(shortCode)
            ?: throw UrlNotFoundException("URL with short URL '$shortCode' not found")
        return url.originalUrl
    }

    fun deleteById(id: UUID) {
        logger.info("Deleting URL by ID: {}", id)
        persistenceService.deleteById(id)
    }

    fun findAllPaginated(page: Int = 0, size: Int = 10): List<UrlGet> {
        logger.info("Finding all URLs with pagination: page={}, size={}", page, size)
        return persistenceService.findAllPaginated(page, size)
    }
}
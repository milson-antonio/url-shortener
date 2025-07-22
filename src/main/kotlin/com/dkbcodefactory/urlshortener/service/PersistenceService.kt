package com.dkbcodefactory.urlshortener.service

import com.dkbcodefactory.urlshortener.dtos.UrlGet
import com.dkbcodefactory.urlshortener.mapper.Mapper
import com.dkbcodefactory.urlshortener.repository.UrlRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import java.util.UUID
import kotlin.jvm.java

@Service
class PersistenceService(
    private val urlRepository: UrlRepository,
    private val redisService: RedisService,
) {
    private val logger = LoggerFactory.getLogger(PersistenceService::class.java)

    fun findUrlByOriginal(originalUrl: String): UrlGet? {
        val cachedUrlOptional = redisService.findByOriginalUrl(originalUrl)
        if (cachedUrlOptional.isPresent) {
            logger.info("URL found in Redis cache: {}", originalUrl)
            return cachedUrlOptional.get()
        }

        val entity = urlRepository.findByOriginalUrl(originalUrl)
        if (entity.isPresent) {
            val urlGet = Mapper.mapToUrlGet(entity.get())
            redisService.saveUrl(urlGet)
            logger.info("URL found in database and cached in Redis: {}", originalUrl)
            return urlGet
        }
        return null
    }

    fun findUrlByShortUrl(shorterUrl: String): UrlGet? {
        val cachedUrlOptional = redisService.findByShortUrl(shorterUrl)
        if (cachedUrlOptional.isPresent) {
            logger.info("URL found in Redis cache by short URL: {}", shorterUrl)
            return cachedUrlOptional.get()
        }

        val entity = urlRepository.findByShorterUrl(shorterUrl)
        if (entity.isPresent) {
            val urlGet = Mapper.mapToUrlGet(entity.get())
            redisService.saveUrl(urlGet)
            logger.info("URL found in database by short URL and cached in Redis: {}", shorterUrl)
            return urlGet
        }
        return null
    }

    fun findById(id: UUID): UrlGet? {
        val cachedUrlOptional = redisService.findById(id)
        if (cachedUrlOptional.isPresent) {
            logger.info("URL found in Redis cache by ID: {}", id)
            return cachedUrlOptional.get()
        }

        val entity = urlRepository.findById(id)
        if (entity.isPresent) {
            val ack = Mapper.mapToUrlGet(entity.get())
            redisService.saveUrl(ack)
            logger.info("URL found in database by ID and cached in Redis: {}", id)
            return ack
        }
        return null
    }

    @Transactional
    fun saveAndPublish(urlAcknowledgement: UrlGet) {
            try {
                redisService.saveUrl(urlAcknowledgement)
                urlRepository.save(Mapper.mapToUrlEntity(urlAcknowledgement))
                logger.info("New URL persisted and sent: {}", urlAcknowledgement.originalUrl)
            } catch (ex: Exception) {
                logger.error("Error in saveAndPublish for URL {}: {}", 
                    urlAcknowledgement.originalUrl, ex.message, ex)
            }
    }

    @Transactional
    fun deleteById(id: UUID) {
        val urlGet = findById(id)
        if (urlGet != null) {
            redisService.deleteUrl(urlGet)
            urlRepository.deleteById(id)
            logger.info("URL deleted: {}", id)
        } else {
            logger.info("URL not found for deletion: {}", id)
        }
    }

    fun findAllPaginated(page: Int = 0, size: Int = 10): List<UrlGet> {
        val pageable = PageRequest.of(page, size)
        val entities = urlRepository.findAllWithShorterUrlAndSuccessStatus(pageable)

        return entities.content.map { entity ->
            Mapper.mapToUrlGet(entity)
        }
    }
}
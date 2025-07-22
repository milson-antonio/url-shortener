package com.dkbcodefactory.urlshortener.service

import com.dkbcodefactory.urlshortener.dtos.UrlGet
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory
import kotlin.jvm.java
import kotlin.text.isNotEmpty

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, UrlGet>
) {
    private val logger = LoggerFactory.getLogger(RedisService::class.java)

    companion object {
        private const val URL_KEY_PREFIX = "url:"
        private const val SHORT_URL_KEY_PREFIX = "short_url:"
        private const val ID_KEY_PREFIX = "id:"
        private const val DEFAULT_EXPIRATION = 24L
    }

    fun findById(id: UUID): Optional<UrlGet> {
        val key = generateIdKey(id)
        val urlGet = redisTemplate.opsForValue().get(key)
        return Optional.ofNullable(urlGet)
    }

    fun findByOriginalUrl(originalUrl: String): Optional<UrlGet> {
        val key = generateKey(originalUrl)
        val urlGet = redisTemplate.opsForValue().get(key)
        return Optional.ofNullable(urlGet)
    }

    fun findByShortUrl(shortUrl: String): Optional<UrlGet> {
        val key = generateShortUrlKey(shortUrl)
        val urlGet = redisTemplate.opsForValue().get(key)
        return if (urlGet != null && urlGet.shorterUrl.isNotEmpty()) {
            Optional.of(urlGet)
        } else {
            Optional.empty()
        }
    }

    private fun generateKey(originalUrl: String): String {
        return "$URL_KEY_PREFIX$originalUrl"
    }

    private fun generateShortUrlKey(shortUrl: String): String {
        return "$SHORT_URL_KEY_PREFIX$shortUrl"
    }

    private fun generateIdKey(id: UUID): String {
        return "$ID_KEY_PREFIX$id"
    }

    fun saveUrl(urlGet: UrlGet) {
        val key = generateKey(urlGet.originalUrl)
        redisTemplate.opsForValue().set(key, urlGet, DEFAULT_EXPIRATION, TimeUnit.HOURS)

        if (urlGet.shorterUrl.isNotEmpty()) {
            val shortKey = generateShortUrlKey(urlGet.shorterUrl)
            redisTemplate.opsForValue().set(shortKey, urlGet, DEFAULT_EXPIRATION, TimeUnit.HOURS)
        }

        val idKey = generateIdKey(urlGet.id)
        redisTemplate.opsForValue().set(idKey, urlGet, DEFAULT_EXPIRATION, TimeUnit.HOURS)
    }

    fun deleteUrl(urlGet: UrlGet) {
        val originalUrlKey = generateKey(urlGet.originalUrl)
        redisTemplate.delete(originalUrlKey)

        if (urlGet.shorterUrl.isNotEmpty()) {
            val shortUrlKey = generateShortUrlKey(urlGet.shorterUrl)
            redisTemplate.delete(shortUrlKey)
        }

        val idKey = generateIdKey(urlGet.id)
        redisTemplate.delete(idKey)

        logger.info("Deleted URL from Redis cache: {}", urlGet.id)
    }
}
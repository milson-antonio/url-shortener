package com.dkbcodefactory.urlshortener.configuration.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.dkbcodefactory.urlshortener.dtos.UrlGet

@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, UrlGet> {
        val template = RedisTemplate<String, UrlGet>()
        template.connectionFactory = connectionFactory

        val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        val jsonSerializer = Jackson2JsonRedisSerializer(objectMapper, UrlGet::class.java)

        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = jsonSerializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = jsonSerializer

        return template
    }
}
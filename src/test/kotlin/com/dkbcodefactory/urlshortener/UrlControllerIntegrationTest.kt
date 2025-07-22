package com.dkbcodefactory.urlshortener

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.core.env.Environment
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

import org.junit.jupiter.api.TestInstance
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.apply
import kotlin.jvm.java

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class UrlControllerIntegrationTest() {

    @Autowired
    private lateinit var environment: Environment

    @Autowired
    private lateinit var restTemplateBuilder: RestTemplateBuilder

    private lateinit var restTemplate: RestTemplate

    private lateinit var serverUrl: String

    data class UrlCreate(val originalUrl: String)

    data class UrlGet(val id: UUID, val originalUrl: String, val shorterUrl: String, val description: String?)

    companion object {
        private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:17-alpine").apply {
            withDatabaseName("url_shortener_test")
            withUsername("test")
            withPassword("test")
            start()
        }

        private val redisContainer = GenericContainer<Nothing>("redis:7-alpine").apply {
            withExposedPorts(6379)
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgresContainer::getUsername)
            registry.add("spring.datasource.password", postgresContainer::getPassword)

            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
        }
    }

    @BeforeAll
    fun init() {
        val port = environment.getProperty("local.server.port") ?: "8081"
        serverUrl = "http://localhost:$port/api/v1/urls"
        restTemplate = restTemplateBuilder
            .defaultHeader("Connection", "close")
            .build()
    }

    @Test
    fun `create and fetch short URL`() {
        val urlCreate = UrlCreate(originalUrl = "https://kotlinlang.org")

        val createResponse = restTemplate.postForEntity(serverUrl, urlCreate, UrlGet::class.java)
        val urlGet = createResponse.body
        assertNotNull(urlGet)
        assertEquals(urlCreate.originalUrl, urlGet.originalUrl)
        assertNotNull(urlGet.shorterUrl)
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)

        val originalUrl = "$serverUrl/${urlGet.shorterUrl}"
        val originalUrlResponse = restTemplate.exchange(
            originalUrl,
            HttpMethod.GET,
            null,
            Void::class.java
        )
        assertEquals(HttpStatus.OK, originalUrlResponse.statusCode)

    }

    @Test
    fun `list paginated urls`() {
        restTemplate.postForEntity(serverUrl, UrlCreate("https://www.dkbcodefactory.com/"), UrlGet::class.java)
        restTemplate.postForEntity(serverUrl, UrlCreate("https://www.youtube.com/@milson-antonio"), UrlGet::class.java)

        val uri = UriComponentsBuilder.fromHttpUrl(serverUrl)
            .queryParam("page", 0)
            .queryParam("size", 10)
            .build().toUri()

        val response: ResponseEntity<List<UrlGet>> = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<UrlGet>>() {}
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        val urls = response.body
        assertNotNull(urls)
        assert(urls.size >= 2)
    }

    @Test
    fun `delete a short url`() {
        val createResponse =
            restTemplate.postForEntity(serverUrl, UrlCreate("https://example.com/delete"), UrlGet::class.java)
        val urlId = createResponse.body?.id ?: error("ID should not be null")

        val deleteUrl = "$serverUrl/$urlId"
        restTemplate.delete(deleteUrl)

        val redirectUrl = "$serverUrl/${createResponse.body?.shorterUrl}"
        val exception = runCatching {
            restTemplate.exchange(
                redirectUrl,
                HttpMethod.GET,
                null,
                Void::class.java
            )
        }.exceptionOrNull()

        assertNotNull(exception)
    }
}
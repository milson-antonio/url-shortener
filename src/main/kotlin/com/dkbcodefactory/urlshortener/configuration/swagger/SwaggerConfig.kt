package com.dkbcodefactory.urlshortener.configuration.swagger

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class SwaggerConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
    }

    @Bean
    fun apiInfo() = mapOf(
        "title" to "URL Shortener API",
        "description" to "API for shortening URLs",
        "version" to "1.0.0",
        "contact" to mapOf(
            "name" to "Coding Challenge from the DKB Code Factory",
            "url" to "https://www.dkbcodefactory.com"
        )
    )

    @Bean
    fun apiEndpoints() = mapOf(
        "/api/v1/urls" to mapOf(
            "post" to mapOf(
                "summary" to "Create a short URL",
                "description" to "Creates a shortened URL from the original URL",
                "requestBody" to mapOf(
                    "content" to mapOf(
                        "application/json" to mapOf(
                            "schema" to mapOf(
                                "type" to "object",
                                "properties" to mapOf(
                                    "originalUrl" to mapOf(
                                        "type" to "string",
                                        "description" to "The original URL to be shortened",
                                        "example" to "https://www.dkbcodefactory.com/"
                                    ),
                                    "description" to mapOf(
                                        "type" to "string",
                                        "description" to "This is a DKB Code Factory URL",
                                        "example" to "This is a DKB Code Factory URL"
                                    )
                                ),
                                "required" to listOf("originalUrl")
                            )
                        )
                    )
                ),
                "responses" to mapOf(
                    "201" to mapOf(
                        "description" to "Created",
                        "content" to mapOf(
                            "application/json" to mapOf(
                                "schema" to mapOf(
                                    "type" to "object",
                                    "properties" to mapOf(
                                        "id" to mapOf(
                                            "type" to "string",
                                            "format" to "uuid",
                                            "description" to "The UUID of the shortened URL"
                                        ),
                                        "originalUrl" to mapOf(
                                            "type" to "string",
                                            "description" to "The original URL"
                                        ),
                                        "shorterUrl" to mapOf(
                                            "type" to "string",
                                            "description" to "The shortened URL"
                                        ),
                                        "description" to mapOf(
                                            "type" to "string",
                                            "description" to "An optional description for the URL"
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            "get" to mapOf(
                "summary" to "List all URLs",
                "description" to "Lists all shortened URLs with pagination",
                "parameters" to listOf(
                    mapOf(
                        "name" to "page",
                        "in" to "query",
                        "description" to "Page number",
                        "schema" to mapOf(
                            "type" to "integer",
                            "default" to 0
                        )
                    ),
                    mapOf(
                        "name" to "size",
                        "in" to "query",
                        "description" to "Page size",
                        "schema" to mapOf(
                            "type" to "integer",
                            "default" to 10
                        )
                    )
                ),
                "responses" to mapOf(
                    "200" to mapOf(
                        "description" to "OK",
                        "content" to mapOf(
                            "application/json" to mapOf(
                                "schema" to mapOf(
                                    "type" to "array",
                                    "items" to mapOf(
                                        "type" to "object",
                                        "properties" to mapOf(
                                            "id" to mapOf(
                                                "type" to "string",
                                                "format" to "uuid",
                                                "description" to "The UUID of the shortened URL"
                                            ),
                                            "originalUrl" to mapOf(
                                                "type" to "string",
                                                "description" to "The original URL"
                                            ),
                                            "shorterUrl" to mapOf(
                                                "type" to "string",
                                                "description" to "The shortened URL"
                                            ),
                                            "description" to mapOf(
                                                "type" to "string",
                                                "description" to "An optional description for the URL"
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ),
        "/api/v1/urls/{shortCode}" to mapOf(
            "get" to mapOf(
                "summary" to "Return a original URL",
                "description" to "Return the the original URL using the short code",
                "parameters" to listOf(
                    mapOf(
                        "name" to "shortCode",
                        "in" to "path",
                        "description" to "The short code of the URL",
                        "required" to true,
                        "schema" to mapOf(
                            "type" to "string"
                        )
                    )
                ),
                "responses" to mapOf(
                    "200" to mapOf(
                        "description" to "OK",
                        "content" to mapOf(
                            "text/plain" to mapOf(
                                "schema" to mapOf(
                                    "type" to "string",
                                    "description" to "The original URL"
                                )
                            )
                        )
                    )
                )
            )
        ),
        "/api/v1/urls/{id}" to mapOf(
            "delete" to mapOf(
                "summary" to "Delete a short URL",
                "description" to "Deletes a shortened URL by ID",
                "parameters" to listOf(
                    mapOf(
                        "name" to "id",
                        "in" to "path",
                        "description" to "The UUID of the shortened URL",
                        "required" to true,
                        "schema" to mapOf(
                            "type" to "string",
                            "format" to "uuid"
                        )
                    )
                ),
                "responses" to mapOf(
                    "204" to mapOf(
                        "description" to "No Content"
                    )
                )
            )
        )
    )
}
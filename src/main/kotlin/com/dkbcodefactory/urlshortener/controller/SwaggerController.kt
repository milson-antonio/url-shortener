package com.dkbcodefactory.urlshortener.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.collections.joinToString
import kotlin.text.trimIndent
import kotlin.to

@RestController
class SwaggerController {

    @Autowired
    private lateinit var apiInfo: Map<String, Any>

    @Autowired
    private lateinit var apiEndpoints: Map<String, Any>

    @GetMapping("/swagger-ui", produces = [MediaType.TEXT_HTML_VALUE])
    fun swaggerUi(): String {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>URL Shortener API - Swagger UI</title>
            <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@4.5.0/swagger-ui.css" />
            <style>
                body {
                    margin: 0;
                    padding: 0;
                }
                #swagger-ui {
                    height: 100vh;
                }
            </style>
        </head>
        <body>
            <div id="swagger-ui"></div>
            <script src="https://unpkg.com/swagger-ui-dist@4.5.0/swagger-ui-bundle.js"></script>
            <script>
                window.onload = function() {
                    const ui = SwaggerUIBundle({
                        spec: {
                            openapi: "3.0.0",
                            info: ${apiInfo.toJsonString()},
                            paths: ${apiEndpoints.toJsonString()}
                        },
                        dom_id: '#swagger-ui',
                        deepLinking: true,
                        presets: [
                            SwaggerUIBundle.presets.apis,
                            SwaggerUIBundle.SwaggerUIStandalonePreset
                        ],
                        layout: "BaseLayout"
                    });
                    window.ui = ui;
                };
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    @GetMapping("/api-docs", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun apiDocs(): Map<String, Any> {
        return mapOf(
            "openapi" to "3.0.0",
            "info" to apiInfo,
            "paths" to apiEndpoints
        )
    }

    private fun Any?.toJsonString(): String {
        return when (this) {
            is Map<*, *> -> {
                val entries = this.entries.joinToString(",") { (key, value) ->
                    "\"$key\": ${value?.toJsonString()}"
                }
                "{$entries}"
            }
            is List<*> -> {
                val items = this.joinToString(",") { it?.toJsonString() ?: "null" }
                "[$items]"
            }
            is String -> "\"$this\""
            is Number -> "$this"
            is Boolean -> "$this"
            null -> "null"
            else -> "\"$this\""
        }
    }
}

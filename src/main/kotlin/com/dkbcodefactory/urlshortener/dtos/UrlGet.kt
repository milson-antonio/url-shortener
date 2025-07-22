package com.dkbcodefactory.urlshortener.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID


@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class UrlGet(
    val id: UUID,
    var originalUrl: String,
    val shorterUrl: String,
    val description: String?
)

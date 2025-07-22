package com.dkbcodefactory.urlshortener.mapper

import com.dkbcodefactory.urlshortener.dtos.UrlGet
import com.dkbcodefactory.urlshortener.model.UrlEntity

object Mapper {
    fun mapToUrlEntity(urlGet: UrlGet): UrlEntity {
        return UrlEntity(
            id = urlGet.id,
            originalUrl = urlGet.originalUrl,
            shorterUrl = urlGet.shorterUrl,
            description = urlGet.description
        )
    }

    fun mapToUrlGet(urlEntity: UrlEntity): UrlGet {
        return UrlGet(
            id = urlEntity.id,
            originalUrl = urlEntity.originalUrl,
            shorterUrl = urlEntity.shorterUrl,
            description = urlEntity.description
        )
    }
}

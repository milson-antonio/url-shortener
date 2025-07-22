package com.dkbcodefactory.urlshortener.repository

import com.dkbcodefactory.urlshortener.model.UrlEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID
@Repository
interface UrlRepository : JpaRepository<UrlEntity, UUID> {

    @Query(value = "SELECT * FROM url_shortener.url WHERE original_url = :originalUrl", nativeQuery = true)
    fun findByOriginalUrl(@Param("originalUrl") originalUrl: String): Optional<UrlEntity>

    @Query(value = "SELECT * FROM url_shortener.url WHERE shorter_url = :shorterUrl AND shorter_url IS NOT NULL", nativeQuery = true)
    fun findByShorterUrl(@Param("shorterUrl") shorterUrl: String): Optional<UrlEntity>

    @Query(
        value = "SELECT * FROM url_shortener.url WHERE shorter_url IS NOT NULL AND shorter_url != ''",
        countQuery = "SELECT COUNT(*) FROM url_shortener.url WHERE shorter_url IS NOT NULL AND shorter_url != ''",
        nativeQuery = true
    )
    fun findAllWithShorterUrlAndSuccessStatus(pageable: Pageable): Page<UrlEntity>

}
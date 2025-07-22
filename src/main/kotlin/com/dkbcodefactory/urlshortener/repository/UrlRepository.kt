package com.dkbcodefactory.urlshortener.repository

import com.dkbcodefactory.urlshortener.model.UrlEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID
@Repository
interface UrlRepository : JpaRepository<UrlEntity, UUID> {
}
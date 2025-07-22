package com.dkbcodefactory.urlshortener.controller

import com.dkbcodefactory.urlshortener.dtos.UrlGet
import com.dkbcodefactory.urlshortener.dtos.UrlCreate
import com.dkbcodefactory.urlshortener.service.UrlService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/api/v1/urls")
@Validated
class UrlController(private val urlService: UrlService) {

    @PostMapping
    fun createShortUrl(@Valid @RequestBody urlCreate: UrlCreate): ResponseEntity<UrlGet> {
        val urlGet = urlService.createShortUrl(urlCreate);

        val location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(urlGet.id)
            .toUri();

        return ResponseEntity.created(location).body(urlGet)
    }

    @GetMapping("/{shortCode}")
    fun redirect(@PathVariable shortCode: String): ResponseEntity<String> {
        val originalUrl = urlService.resolveShortCode(shortCode)
        return ResponseEntity.status(HttpStatus.OK).body(originalUrl)
    }

    @DeleteMapping("/{id}")
    fun deleteShortUrl(@PathVariable id: UUID): ResponseEntity<Void> {
        urlService.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun listUrls(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<List<UrlGet>> {
        return ResponseEntity.ok(urlService.findAllPaginated(page, size))
    }
}
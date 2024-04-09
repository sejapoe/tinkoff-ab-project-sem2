package ru.sejapoe.tinkab.integration.rest

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import ru.sejapoe.tinkab.config.AbstractBaseTest

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractRestTest : AbstractBaseTest() {
    companion object {
        const val HOST: String = "http://localhost"
        const val BASE_URL: String = "/api/v1"
    }

    @LocalServerPort
    private val port = 0

    protected val restTemplate: TestRestTemplate = TestRestTemplate()

    protected val httpHeaders: HttpHeaders = HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
    }

    protected fun createURLWithPort(uri: String): String {
        return "$HOST:$port$BASE_URL$uri"
    }
}
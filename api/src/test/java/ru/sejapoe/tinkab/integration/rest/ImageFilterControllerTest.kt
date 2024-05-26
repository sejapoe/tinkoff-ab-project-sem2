package ru.sejapoe.tinkab.integration.rest

import jakarta.servlet.http.Cookie
import org.apache.tomcat.util.http.Rfc6265CookieProcessor
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.core.io.ClassPathResource
import org.springframework.http.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import ru.sejapoe.tinkab.controller.AuthController
import ru.sejapoe.tinkab.security.JwtService
import java.time.Duration
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ImageFilterControllerTest : AbstractRestTest() {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var jwtService: JwtService

    private val cookieProcessor = Rfc6265CookieProcessor()

    private val authorizedHttpHeaders: HttpHeaders by lazy {
        HttpHeaders().apply {
            addAll(httpHeaders)
            val token = jwtService.generateToken(mapOf(), "username", Duration.ofDays(10))
            val cookie = Cookie(AuthController.AUTH_COOKIE_NAME, token)
            cookie.isHttpOnly = true
            cookie.path = "/"
            cookie.secure = true
            cookie.maxAge = Math.toIntExact(Duration.ofDays(10).seconds)
            cookie.toString()
            val generateHeader = cookieProcessor.generateHeader(cookie, null)
            add(HttpHeaders.COOKIE, generateHeader)
            contentType = null
        }
    }
    private val jsonAuthorizedHttpHeader by lazy {
        HttpHeaders().apply {
            addAll(authorizedHttpHeaders)
            contentType = MediaType.APPLICATION_JSON
        }
    }

    private lateinit var storedUUID: UUID
    private lateinit var requestUUID: UUID

    @Suppress("SqlWithoutWhere")
    @BeforeAll
    fun setUp() {
        jdbcTemplate.update("DELETE FROM users")
        jdbcTemplate.update("DELETE FROM images")

        jdbcTemplate.update(
            "INSERT INTO users VALUES (default, ?, ?)",
            "username",
            "password"
        )
    }

    @Test
    @Order(0)
    fun testAuthorizedHttpHeaders() {
        assertThat(authorizedHttpHeaders["Cookie"]).isNotNull.hasSize(1)
    }

    @Test
    @Order(1)
    fun uploadImage() {
        val file = ClassPathResource("files/image.jpeg")

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file", file)

        val requestEntity = HttpEntity(
            body,
            HttpHeaders().apply {
                addAll(authorizedHttpHeaders)
                contentType = MediaType.MULTIPART_FORM_DATA
            }
        )

        val responseEntity = restTemplate.postForEntity<String>(
            createURLWithPort("/image"),
            requestEntity,
        )

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        val imageId = assertDoesNotThrow {
            JSONObject(responseEntity.body).getString("imageId")
        }
        storedUUID = assertDoesNotThrow {
            UUID.fromString(imageId.toString())
        }
    }

    @Test
    @Order(2)
    fun createRequest() {
        val requestEntity = HttpEntity(
            """
            {
                "filters": [
                    { "type": "CROP", "params": { "rect": [0, 240, 1024, 640] } }
                ]
            }
        """.trimIndent(),
            jsonAuthorizedHttpHeader
        )

        val responseEntity = restTemplate.postForEntity<String>(
            createURLWithPort("/image/$storedUUID/filters/apply"),
            requestEntity,
        )

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        val requestId = assertDoesNotThrow {
            JSONObject(responseEntity.body).getString("requestId")
        }
        requestUUID = assertDoesNotThrow {
            UUID.fromString(requestId)
        }
    }

    @Test
    @Order(3)
    fun createRequestImageNotFound() {
        val requestEntity = HttpEntity(
            """
            {
                "filters": [
                    { "type": "CROP", "params": { "rect": [0, 240, 1024, 640] } }
                ]
            }
        """.trimIndent(),
            jsonAuthorizedHttpHeader
        )
        val randomUUID = UUID.randomUUID()
        val responseEntity = restTemplate.postForEntity<String>(
            createURLWithPort("/image/$randomUUID/filters/apply"),
            requestEntity,
        )

        val expected = """
            {
                "success": false,
                "message": "Image [${randomUUID}] is not found"
            }
        """.trimIndent()

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @Order(4)
    fun createRequestBadFilter() {
        val requestEntity = HttpEntity(
            """
            {
                "filters": [
                    { "type": "PURR" }
                ]
            }
        """.trimIndent(),
            jsonAuthorizedHttpHeader
        )
        val responseEntity = restTemplate.postForEntity<String>(
            createURLWithPort("/image/$storedUUID/filters/apply"),
            requestEntity,
        )

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.statusCode)
    }

    @Test
    @Order(5)
    fun getRequest() {
        val requestEntity = HttpEntity<Nothing>(authorizedHttpHeaders)

        val responseEntity = restTemplate.exchange<String>(
            createURLWithPort("/image/$storedUUID/filters/$requestUUID"),
            HttpMethod.GET,
            requestEntity,
        )

        val expected = """
            {
                "imageId": "$storedUUID",
                "status": "WIP"
            }
        """.trimIndent()

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @Order(7)
    fun getRequestNotFound() {
        val requestEntity = HttpEntity<Nothing>(authorizedHttpHeaders)

        val randomUUID = UUID.randomUUID()
        val responseEntity = restTemplate.exchange<String>(
            createURLWithPort("/image/$storedUUID/filters/$randomUUID"),
            HttpMethod.GET,
            requestEntity,
        )

        val expected = """
            {
                "success": false,
                "message": "Request [${randomUUID}] is not found"
            }
        """.trimIndent()

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @Order(7)
    fun getRequestImageNotFound() {
        val requestEntity = HttpEntity<Nothing>(authorizedHttpHeaders)

        val randomUUID = UUID.randomUUID()
        val responseEntity = restTemplate.exchange<String>(
            createURLWithPort("/image/$randomUUID/filters/$requestUUID"),
            HttpMethod.GET,
            requestEntity,
        )

        val expected = """
            {
                "success": false,
                "message": "Image [${randomUUID}] is not found"
            }
        """.trimIndent()

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }
}
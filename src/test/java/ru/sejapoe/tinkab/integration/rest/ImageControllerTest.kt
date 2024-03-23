package ru.sejapoe.tinkab.integration.rest

import jakarta.servlet.http.Cookie
import org.apache.tomcat.util.http.Rfc6265CookieProcessor
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
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
class ImageControllerTest : AbstractRestTest() {
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
        }
    };

    private val getAuthorizedHttpHeaders: HttpHeaders by lazy {
        HttpHeaders().apply {
            addAll(authorizedHttpHeaders)
            contentType = null
        }
    }

    private val anotherAuthorizedHttpHeaders: HttpHeaders by lazy {
        HttpHeaders().apply {
            addAll(httpHeaders)
            val token = jwtService.generateToken(mapOf(), "username2", Duration.ofDays(10))
            val cookie = Cookie(AuthController.AUTH_COOKIE_NAME, token)
            cookie.isHttpOnly = true
            cookie.path = "/"
            cookie.secure = true
            cookie.maxAge = Math.toIntExact(Duration.ofDays(10).seconds)
            cookie.toString()
            val generateHeader = cookieProcessor.generateHeader(cookie, null)
            add(HttpHeaders.COOKIE, generateHeader)
        }
    };

    private val getAnotherAuthorizedHttpHeaders: HttpHeaders by lazy {
        HttpHeaders().apply {
            addAll(anotherAuthorizedHttpHeaders)
            contentType = null
        }
    }

    private lateinit var storedUUID: UUID;

    @BeforeAll
    fun setUp() {
        jdbcTemplate.update("DELETE FROM users")
        jdbcTemplate.update("DELETE FROM images")

        jdbcTemplate.update(
            "INSERT INTO users VALUES (default, ?, ?)",
            "username",
            "password"
        )

        jdbcTemplate.update(
            "INSERT INTO users VALUES (default, ?, ?)",
            "username2",
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
            HttpHeaders(authorizedHttpHeaders).apply {
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
    fun uploadImageWrongType() {
        val file = ClassPathResource("files/test.txt")

        val body: MultiValueMap<String, Any> = LinkedMultiValueMap()
        body.add("file", file)

        val requestEntity = HttpEntity(
            body,
            HttpHeaders(authorizedHttpHeaders).apply {
                contentType = MediaType.MULTIPART_FORM_DATA
            }
        )

        val responseEntity = restTemplate.postForEntity<String>(
            createURLWithPort("/image"),
            requestEntity,
        )

        val expected = """
            {
                "success": false,
                "message": "Bad Request: file - Not acceptable file's content type"
            }
        """.trimIndent()

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @Order(3)
    fun getImage() {
        val file = ClassPathResource("files/image.jpeg")

        val responseEntity = restTemplate.exchange<ByteArray>(
            createURLWithPort("/image/$storedUUID"),
            HttpMethod.GET,
            HttpEntity<Nothing>(getAuthorizedHttpHeaders)
        )

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        assertArrayEquals(file.contentAsByteArray, responseEntity.body)
    }

    @Test
    @Order(4)
    fun getImageNotExists() {
        val randomUUID = UUID.randomUUID()
        val responseEntity = restTemplate.exchange<String>(
            createURLWithPort("/image/$randomUUID"),
            HttpMethod.GET,
            HttpEntity<Nothing>(getAuthorizedHttpHeaders)
        )

        val expected = """
            {
                "success": false,
                "message": "Image [$randomUUID] is not found"
            }
        """.trimIndent()

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @Order(5)
    fun getImageUnauthorized() {
        val responseEntity = restTemplate.exchange<ByteArray>(
            createURLWithPort("/image/$storedUUID"),
            HttpMethod.GET,
        )

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.statusCode)
    }

    @Test
    @Order(6)
    fun getImageWithOtherUser() {
        val responseEntity = restTemplate.exchange<String>(
            createURLWithPort("/image/$storedUUID"),
            HttpMethod.GET,
            HttpEntity<Nothing>(getAnotherAuthorizedHttpHeaders)
        )

        val expected = """
            {
                "success": false,
                "message": "Image [$storedUUID] is not found"
            }
        """.trimIndent()

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @Order(7)
    fun getImages() {
        val responseEntity = restTemplate.exchange<String>(
            createURLWithPort("/images"),
            HttpMethod.GET,
            HttpEntity<Nothing>(
                HttpHeaders(authorizedHttpHeaders).apply {
                    contentType = null
                    accept = listOf(MediaType.APPLICATION_JSON)
                }
            )
        )

        val expected = """
            {
              images: [
                  {
                      "imageId": "$storedUUID",
                      "filename": "image.jpeg",
                      "size": 12201
                  }
              ]
            }
        """.trimIndent()

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @Order(8)
    fun removeImage() {
        val responseEntity = restTemplate.exchange<String>(
            createURLWithPort("/image/$storedUUID"),
            HttpMethod.DELETE,
            HttpEntity<Nothing>(getAuthorizedHttpHeaders)
        )

        val expected = """
            {
                "success": true,
                "message": "Image [$storedUUID] has been deleted"
            }
        """.trimIndent()

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @Order(9)
    fun removeImageNotExists() {
        val randomUUID = UUID.randomUUID()
        val responseEntity = restTemplate.exchange<String>(
            createURLWithPort("/image/$randomUUID"),
            HttpMethod.GET,
            HttpEntity<Nothing>(getAuthorizedHttpHeaders)
        )

        val expected = """
            {
                "success": false,
                "message": "Image [$randomUUID] is not found"
            }
        """.trimIndent()

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @Order(10)
    fun removeImageUnauthorized() {
        val responseEntity = restTemplate.exchange<ByteArray>(
            createURLWithPort("/image/$storedUUID"),
            HttpMethod.GET,
        )

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.statusCode)
    }

    @Test
    @Order(11)
    fun removeImageWithOtherUser() {
        val responseEntity = restTemplate.exchange<String>(
            createURLWithPort("/image/$storedUUID"),
            HttpMethod.GET,
            HttpEntity<Nothing>(getAnotherAuthorizedHttpHeaders)
        )

        val expected = """
            {
                "success": false,
                "message": "Image [$storedUUID] is not found"
            }
        """.trimIndent()

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @Order(12)
    fun getImagesAfterRemove() {
        val responseEntity = restTemplate.exchange<String>(
            createURLWithPort("/images"),
            HttpMethod.GET,
            HttpEntity<Nothing>(
                HttpHeaders(authorizedHttpHeaders).apply {
                    contentType = null
                    accept = listOf(MediaType.APPLICATION_JSON)
                }
            )
        )

        val expected = """
            {
              images: []
            }
        """.trimIndent()

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }
}
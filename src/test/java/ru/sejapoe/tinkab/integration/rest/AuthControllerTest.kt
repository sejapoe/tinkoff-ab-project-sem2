package ru.sejapoe.tinkab.integration.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder

class AuthControllerTest : AbstractRestTest() {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setUp() {
        @Suppress("SqlWithoutWhere")
        jdbcTemplate.update("DELETE FROM users")
    }

    @Test
    @DisplayName("Test login success")
    fun loginSuccess() {
        jdbcTemplate.update(
            "INSERT INTO users VALUES (default, ?, ?)",
            "correctUsername",
            passwordEncoder.encode("correctPassword")
        )

        val requestEntity = HttpEntity(
            """
            {
                "username": "correctUsername",
                "password": "correctPassword"
            }
            """, httpHeaders
        )

        val responseEntity = restTemplate.exchange(
            createURLWithPort("/auth/login"),
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        val expected = """
            {
                "success": true,
                "message": "Authorized"
            }
            """

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
        val setCookieHeader = responseEntity.headers["Set-Cookie"]
        assertThat(setCookieHeader).isNotNull.anyMatch {
            it.startsWith("token=")
        }
    }


    @Test
    @DisplayName("Test login bad request")
    fun loginBadRequest() {
        val requestEntity = HttpEntity(
            """
            {
                "username": "",
                "password": ""
            }
            """, httpHeaders
        )

        val responseEntity = restTemplate.exchange(
            createURLWithPort("/auth/login"),
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)
    }


    @Test
    @DisplayName("Test login failed")
    fun loginFailed() {
        val requestEntity = HttpEntity(
            """
            {
                "username": "username",
                "password": "password"
            }
            """, httpHeaders
        )

        val responseEntity = restTemplate.exchange(
            createURLWithPort("/auth/login"),
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        val expected = """
            {
                "success": false,
                "message": "Wrong username or password"
            }
            """

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @DisplayName("Test register success")
    fun register() {
        val requestEntity = HttpEntity(
            """
            {
                "username": "username",
                "password": "password"
            }
            """, httpHeaders
        )

        val responseEntity = restTemplate.exchange(
            createURLWithPort("/auth/register"),
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        val expected = """
            {
                "success": true,
                "message": "Authorized"
            }
            """

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
        val setCookieHeader = responseEntity.headers["Set-Cookie"]
        assertThat(setCookieHeader).isNotNull.anyMatch {
            it.startsWith("token=")
        }
    }

    @Test
    @DisplayName("Test register failed")
    fun registerFailed() {
        jdbcTemplate.update(
            "INSERT INTO users VALUES (default, ?, ?)",
            "correctUsername",
            passwordEncoder.encode("correctPassword")
        )

        val requestEntity = HttpEntity(
            """
            {
                "username": "correctUsername",
                "password": "password"
            }
            """, httpHeaders
        )

        val responseEntity = restTemplate.exchange(
            createURLWithPort("/auth/register"),
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        val expected = """
            {
                "success": false,
                "message": "User with that name already exists"
            }
            """

        assertEquals(HttpStatus.CONFLICT, responseEntity.statusCode)
        JSONAssert.assertEquals(expected, responseEntity.body, JSONCompareMode.STRICT)
    }

    @Test
    @DisplayName("Test register bad request")
    fun registerBadRequest() {
        val requestEntity = HttpEntity(
            """
            {
                "username": "x",
                "password": "x"
            }
            """, httpHeaders
        )

        val responseEntity = restTemplate.exchange(
            createURLWithPort("/auth/register"),
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.statusCode)
    }
}

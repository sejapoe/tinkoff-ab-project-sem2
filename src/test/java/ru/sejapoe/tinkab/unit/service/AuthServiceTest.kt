package ru.sejapoe.tinkab.unit.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.NoOpPasswordEncoder
import ru.sejapoe.tinkab.exception.ConflictException
import ru.sejapoe.tinkab.exception.UnauthorizedException
import ru.sejapoe.tinkab.repo.user.UserRepository
import ru.sejapoe.tinkab.security.JwtService
import ru.sejapoe.tinkab.service.AuthService

internal class AuthServiceTest {
    @Test
    @DisplayName("Test login success")
    fun login() {
        val authenticationManager = mock<AuthenticationManager> {
            on { authenticate(any()) } doReturn null
        }

        val jwtService = mock<JwtService> {
            on { generateToken(any(), any(), any()) } doReturn "mock-token"
        }

        val authService = AuthService(mock(), jwtService, authenticationManager, mock())
        val token = authService.login("username", "password")

        verify(authenticationManager, times(1)).authenticate(any())
        verify(jwtService, times(1))
            .generateToken(any(), any(), any())
        assertThat(token).isEqualTo("mock-token")
    }

    @Test
    @DisplayName("Test login failed")
    fun loginFailed() {
        val authenticationManager = mock<AuthenticationManager> {
            on { authenticate(any()) } doThrow UsernameNotFoundException::class
        }

        val jwtService = mock<JwtService> {
            on { generateToken(any(), any(), any()) } doReturn "mock-token"
        }

        val authService = AuthService(mock(), jwtService, authenticationManager, mock())
        assertThatThrownBy {
            authService.login("username", "password")
        }.isInstanceOf(UnauthorizedException::class.java)
            .message().isEqualTo("Wrong username or password")

        verify(authenticationManager, times(1)).authenticate(any())
        verify(jwtService, times(0))
            .generateToken(any(), any(), any())
    }

    @Test
    @DisplayName("Test register success")
    fun register() {
        val passwordEncoder = NoOpPasswordEncoder.getInstance()

        val userRepository = mock<UserRepository> {
            on { findByUsername(any()) } doThrow EmptyResultDataAccessException::class
            on { add(any(), any()) } doReturn null
        }

        val jwtService = mock<JwtService> {
            on { generateToken(any(), any(), any()) }.thenReturn("mock-token")
        }

        val authService = AuthService(userRepository, jwtService, mock(), passwordEncoder)
        authService.register("username", "password")

        verify(userRepository, times(1)).findByUsername("username")
        verify(userRepository, times(1)).add("username", "password")
        verify(jwtService, times(1))
            .generateToken(any(), any(), any())
    }

    @Test
    @DisplayName("Test register failed")
    fun registerFailed() {
        val passwordEncoder = NoOpPasswordEncoder.getInstance()

        val userRepository = mock<UserRepository> {
            on { findByUsername(any()) } doReturn null
            on { add(any(), any()) } doReturn null
        }

        val jwtService = mock<JwtService> {
            on { generateToken(any(), any(), any()) } doReturn "mock-token"
        }

        val authService = AuthService(userRepository, jwtService, mock(), passwordEncoder)
        assertThatThrownBy {
            authService.register("username", "password")
        }.isInstanceOf(ConflictException::class.java).message().isEqualTo("User with that name already exists")

        verify(userRepository, times(1)).findByUsername("username")
        verify(userRepository, times(0)).add("username", "password")
        verify(jwtService, times(0))
            .generateToken(any(), any(), any())
    }
}
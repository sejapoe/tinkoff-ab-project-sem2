package ru.sejapoe.tinkab.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sejapoe.tinkab.dto.SuccessResponse;
import ru.sejapoe.tinkab.dto.auth.LoginRequest;
import ru.sejapoe.tinkab.service.AuthService;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    public static final String AUTH_COOKIE_NAME = "token";
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public SuccessResponse login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        var token = authService.login(loginRequest.username(), loginRequest.password());
        addCookie(token, response);
        return new SuccessResponse(true, "Authorized");
    }

    @PostMapping("/register")
    public SuccessResponse register(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        var token = authService.register(loginRequest.username(), loginRequest.password());
        addCookie(token, response);
        return new SuccessResponse(true, "Authorized");
    }

    private void addCookie(String token, HttpServletResponse response) {
        var cookie = new Cookie(AUTH_COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setMaxAge(Math.toIntExact(Duration.ofDays(10).getSeconds()));
        response.addCookie(cookie);
    }
}

package ru.sejapoe.tinkab.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.sejapoe.tinkab.exception.ConflictException;
import ru.sejapoe.tinkab.exception.UnauthorizedException;
import ru.sejapoe.tinkab.repo.user.UserRepository;
import ru.sejapoe.tinkab.security.JwtService;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public String login(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Wrong username or password");
        }
        return jwtService.generateToken(Map.of(), username, Duration.ofDays(10));
    }

    public String register(String username, String password) {
        try {
            userRepository.findByUsername(username);
            throw new ConflictException("User with that name already exists");
        } catch (EmptyResultDataAccessException e) {
            userRepository.add(username, passwordEncoder.encode(password));
            return jwtService.generateToken(Map.of(), username, Duration.ofDays(10));
        }
    }
}

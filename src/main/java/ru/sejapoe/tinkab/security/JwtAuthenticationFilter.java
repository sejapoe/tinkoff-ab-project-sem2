package ru.sejapoe.tinkab.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTH_COOKIE_NAME = "token";
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        log.info("Doing authorization");
        try {
            handleToken(request);
        } catch (Exception exception) {
            log.debug("Unauthorized", exception);
        }
        filterChain.doFilter(request, response);
    }

    private void handleToken(@NotNull HttpServletRequest request) {
        var token = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(AUTH_COOKIE_NAME))
                .findFirst()
                .orElseThrow()
                .getValue();

        var username = jwtService.extractSubject(token);
        log.info("Authorized as " + username);

        if (SecurityContextHolder.getContext().getAuthentication() != null) return;

        var authentication = new UsernamePasswordAuthenticationToken(username, null,
                List.of(new SimpleGrantedAuthority("USER")));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

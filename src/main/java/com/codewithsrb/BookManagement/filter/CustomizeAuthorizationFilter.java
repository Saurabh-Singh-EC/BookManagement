package com.codewithsrb.BookManagement.filter;

import com.codewithsrb.BookManagement.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static util.ExceptionUtils.processError;

/**
 * Custom authorization filter which is responsible for validating the token and setting
 * the authenticated user in the spring context holder.
 */
@Component
@Slf4j
public class CustomizeAuthorizationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String EMAIL_KEY = "email";
    private static final String TOKEN_KEY = "token";
    private static final String[] PUBLIC_ROUTES = {"/books/register", "/books/login"};
    private static final String HTTP_OPTIONS_METHOD = "OPTIONS";

    private final TokenProvider tokenProvider;

    public CustomizeAuthorizationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {

        try {
            Map<String, String> values = getRequestValues(request);
            String token = values.get(TOKEN_KEY);
            String email = values.get(EMAIL_KEY);
            if(tokenProvider.isTokenValid(email, token)) {
                List<GrantedAuthority> grantedAuthorities = tokenProvider.getGrantedAuthorities(token);
                Authentication authentication = tokenProvider.getAuthentication(email, grantedAuthorities, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
            filterChain.doFilter(request, response);
        } catch(Exception exception) {
            log.error(exception.getMessage());
            processError(response, exception);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getHeader(AUTHORIZATION) == null
                || !request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX)
                || request.getHeader(AUTHORIZATION).equalsIgnoreCase(HTTP_OPTIONS_METHOD)
                || asList(PUBLIC_ROUTES).contains(request.getRequestURI());
    }

    private Map<String, String> getRequestValues(HttpServletRequest request) {
        String token = getToken(request);
        return Map.of(EMAIL_KEY, tokenProvider.getSubject(token, request), TOKEN_KEY, token);
    }

    private String getToken(HttpServletRequest request) {
        return ofNullable(request.getHeader(AUTHORIZATION))
                .filter(header -> header.startsWith(TOKEN_PREFIX))
                .map(header -> header.replace(TOKEN_PREFIX, EMPTY))
                .get();
    }
}

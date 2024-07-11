package com.codewithsrb.BookManagement.provider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.codewithsrb.BookManagement.model.UserDetailsImpl;
import com.codewithsrb.BookManagement.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Token provider class which is responsible for creating the access token
 * It also provides features to validate the token and get the relevant information from the token.
 */
@Component
@Slf4j
public class TokenProvider {

    private static final String AUTHORITIES = "authorities";
    private static final String CODE_WITH_SRB_LLC = "CODE_WITH_SRB_LLC";
    private static final String BOOKING_MANAGEMENT_SERVICE = "BOOK_MANAGEMENT_SERVICE";
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1_800_000;

    @Value("${jwt.secret}")
    private String secret;

    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public TokenProvider(UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    public String createAccessToken(UserDetailsImpl userDetailsImpl) {
        String[] claims = getClaimsFromUser(userDetailsImpl);
        return JWT.create()
                .withIssuer(CODE_WITH_SRB_LLC)
                .withAudience(BOOKING_MANAGEMENT_SERVICE)
                .withIssuedAt(new Date())
                .withSubject(userDetailsImpl.getUsername())
                .withArrayClaim(AUTHORITIES, claims)
                .withExpiresAt(new Date(currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));
    }

    private static String[] getClaimsFromUser(UserDetailsImpl userDetailsImpl) {
        return userDetailsImpl.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray(String[]::new);
    }

    public List<GrantedAuthority> getGrantedAuthorities(String token) {
        String[] claims = getClaimsFromToken(token);
        return stream(claims).map(SimpleGrantedAuthority::new).collect(toList());
    }

    private String[] getClaimsFromToken(String token) {
        JWTVerifier verifier = getJwtVerifier();
        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);
    }

    private JWTVerifier getJwtVerifier() {
        try {
            Algorithm algorithm = HMAC512(secret);
            return JWT.require(algorithm).withIssuer(CODE_WITH_SRB_LLC).build();
        } catch (JWTVerificationException e) {
            throw new JWTVerificationException("Token Can not be verified");
        }
    }

    public Authentication getAuthentication(String email, List<GrantedAuthority> grantedAuthorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken userPasswordAuthToken = new UsernamePasswordAuthenticationToken(userDetailsServiceImpl.findUserByEmail(email), null, grantedAuthorities);
        userPasswordAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return userPasswordAuthToken;
    }

    public boolean isTokenValid(String email, String token) {
        return StringUtils.isNotEmpty(email) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiresAt = getJwtVerifier().verify(token).getExpiresAt();
        return expiresAt.before(new Date());
    }

    public String getSubject(String token, HttpServletRequest request) {
        try {
            return getJwtVerifier().verify(token).getSubject();
        } catch (TokenExpiredException exception) {
            request.setAttribute("expiredMessage", exception.getMessage());
            throw exception;
        } catch (InvalidClaimException exception) {
            request.setAttribute("invalidClaim", exception.getMessage());
            throw exception;
        }
    }
}

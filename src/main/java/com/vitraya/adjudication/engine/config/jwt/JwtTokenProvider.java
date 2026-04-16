package com.vitraya.adjudication.engine.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j

/**
 * This class provides methods for generating, validating, and parsing JWT tokens.
 */
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access.token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.token.expiration}")
    private long refreshTokenExpiration;

    /**
     * Generates an access token for the given username and claims.
     *
     * @param username the username for which the token is generated
     * @param claims   additional claims to be included in the token
     * @return the generated access token
     */
    public String generateAccessToken(String username, Map<String, Object> claims) {
        return generateToken(username, claims, accessTokenExpiration);
    }

    /**
     * Generates a refresh token for the given username.
     *
     * @param username the username for which the token is generated
     * @param claims
     * @return the generated refresh token
     */
    public String generateRefreshToken(String username, Map<String, Object> claims) {
        return generateToken(username, claims, refreshTokenExpiration);
    }

    /**
     * Generates a token for the given username, claims, and expiration time.
     *
     * @param username   the username for which the token is generated
     * @param claims     additional claims to be included in the token
     * @param expiration the expiration time of the token in milliseconds
     * @return the generated token
     */
    private String generateToken(String username, Map<String, Object> claims, long expiration) {
        // First set all claims
        JwtBuilder jwtBuilder = Jwts.builder()
                .setClaims(claims);

        // Then add additional claims that might not be in the claims map
        jwtBuilder.setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, getSigningKey());

        return jwtBuilder.compact();
    }

    /**
     * Retrieves the signing key from the JWT secret.
     *
     * @return the signing key
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the username from the given token.
     *
     * @param token the JWT token
     * @return the username extracted from the token
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).get("username").toString();
    }

    /**
     * Extracts the claims from the given token.
     *
     * @param token the JWT token
     * @return the claims extracted from the token
     */
    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.info("JWT Token {} expired at {}", token, e.getClaims().getExpiration());
            throw new JwtException("Token expired");
        }
    }

    /**
     * Validates the given token.
     *
     * @param token the JWT token
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                log.info("Received token is null or blank");
                return false;
            }
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.info("Caught exception while validating token", e);
            return false;
        }
    }

    /**
     * Validates the given token against the provided user details.
     *
     * @param token       the JWT token
     * @param userDetails the user details to validate against
     * @return true if the token is valid and matches the user details, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks if the given token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the given token.
     *
     * @param token the JWT token
     * @return the expiration date extracted from the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the given token using the provided claims resolver.
     *
     * @param token          the JWT token
     * @param claimsResolver the function to resolve the claim
     * @param <T>            the type of the claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts the JWT token from the request's Authorization header.
     *
     * @param request the HTTP request
     * @return the extracted JWT token, or null if not found
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7).trim();
        }
        return null;
    }
}

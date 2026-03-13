package dev.resource_server.global.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class JwtUtils {

    private final SecretKey secretKey;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtUtils(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 토큰 생성
     * userId claims에 올림
     */
    public String createToken(Long userId, Instant expiredAt) {
        return Jwts.builder()
                   .claim("userId", userId)
                   .issuedAt(new Date())
                   .expiration(Date.from(expiredAt))
                   .signWith(secretKey)
                   .compact();
    }

    public String getTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (!(StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX))) {
            return null;
        }
        return bearerToken.substring(BEARER_PREFIX.length());
    }

    /**
     * 토큰에 userId가 있는지 검증
     */
    public boolean isValidateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 JWT 토큰 error={}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Long.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
